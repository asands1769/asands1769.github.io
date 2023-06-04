package org.rally.backend.userprofilearm.controller;

import org.rally.backend.eventsarm.models.Event;
import org.rally.backend.eventsarm.repository.EventRepository;
import org.rally.backend.forumarm.models.ForumPosts;
import org.rally.backend.forumarm.models.Replies;
import org.rally.backend.forumarm.repository.ForumPostRepository;
import org.rally.backend.forumarm.repository.RepliesRepository;
import org.rally.backend.servicesarm.repository.ServiceRepository;
import org.rally.backend.userprofilearm.exception.MinimumCharacterException;
import org.rally.backend.userprofilearm.model.*;
import org.rally.backend.userprofilearm.model.dto.DirectMessageDTO;
import org.rally.backend.userprofilearm.model.dto.HidePostDTO;
import org.rally.backend.userprofilearm.model.dto.UserInfoDTO;
import org.rally.backend.userprofilearm.model.response.ResponseMessage;
import org.rally.backend.userprofilearm.model.UserPostHistory;
import org.rally.backend.userprofilearm.repository.*;
import org.rally.backend.userprofilearm.utility.ImageUtility;
import org.rally.backend.userprofilearm.utility.UserProfileControllerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "/user")
public class UserProfileController {

    UserRepository userRepository;
    UserInformationRepository userInformationRepository;
    RoleRepository roleRepository;
    DirectMessageRepository directMessageRepository;
    ProfilePictureRepository profilePictureRepository;
    ForumPostRepository forumPostRepository;
    RepliesRepository repliesRepository;
    HiddenPostRepository hiddenPostRepository;
    ServiceRepository serviceRepository;
    EventRepository eventRepository;


    @Autowired
    public UserProfileController(UserRepository userRepository, RoleRepository roleRepository,
                                 UserInformationRepository userInformationRepository,
                                 DirectMessageRepository directMessageRepository, ProfilePictureRepository profilePictureRepository,
                                 ForumPostRepository forumPostRepository, RepliesRepository repliesRepository,
                                 HiddenPostRepository hiddenPostRepository, ServiceRepository serviceRepository,
                                 EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userInformationRepository = userInformationRepository;
        this.directMessageRepository = directMessageRepository;
        this.profilePictureRepository = profilePictureRepository;
        this.forumPostRepository = forumPostRepository;
        this.repliesRepository = repliesRepository;
        this.hiddenPostRepository = hiddenPostRepository;
        this.serviceRepository = serviceRepository;
        this.eventRepository = eventRepository;
    }

    /** GET REQUEST **/
    /** GET REQUEST **/
    /** GET REQUEST **/


    @GetMapping("/search")
    public List<UserEntity> displayAllUsers() {
        return this.userRepository.findAll();
    }

    @GetMapping("/getViewUserBundleInformation/{userName}")
    public ResponseEntity<?> getViewUserInformation(@PathVariable String userName) {

        Optional<UserEntity> areYouThere = Optional.ofNullable(userRepository.findByUserName(userName));
        if (areYouThere.isEmpty()) {
            return new ResponseEntity<>(new ResponseMessage("404"), HttpStatus.OK);
        }

        /** Find user and set objects to send to the front **/
        UserEntity targetUser = userRepository.findByUserName(userName);
        UserBundle viewUserProfileBundle = new UserBundle();

        /** Find viewing users information **/
        viewUserProfileBundle.setViewUser(userRepository.findByUserName(userName));
        viewUserProfileBundle.setViewUserInformation(userInformationRepository.findByUserId(targetUser.getId()));
        viewUserProfileBundle.setViewUserDmHistory(UserProfileControllerService.activeUserDirectMessageHistory(targetUser.getId()));
        viewUserProfileBundle.setUpdatedPostHistoryViewUser(UserProfileControllerService.sortUpdatedPostHistoryViewUser(targetUser.getId()));


        return new ResponseEntity<>(viewUserProfileBundle, HttpStatus.OK);

    }

    @GetMapping("/getMainUserBundleInformation/{userName}")
    public ResponseEntity<?> getMainUserBundle(@PathVariable String userName) {

        Optional<UserEntity> areYouThere = Optional.ofNullable(userRepository.findByUserName(userName));
        if (areYouThere.isEmpty()) {
            return new ResponseEntity<>(new ResponseMessage("404"), HttpStatus.OK);
        }

        UserEntity targetUser = userRepository.findByUserName(userName);
        Optional<UserInformation> targetInformation = userInformationRepository.findByUserId(targetUser.getId());
        UserDmHistory targetDirectMessages = UserProfileControllerService.activeUserDirectMessageHistory(targetUser.getId());
        UserPostHistory targetUserPostHistory = new UserPostHistory();

        List<HiddenPost> targetHiddenPost = UserProfileControllerService.getHiddenPostListForUserBundleMain(targetUser.getId());
        targetUserPostHistory.setViewUserHiddenPost(targetHiddenPost);
        List<ForumPosts> targetForumPost = UserProfileControllerService.getUserForumPost(targetUser.getId());
        targetUserPostHistory.setViewUserForumPost(targetForumPost);
        List<Replies> targetForumReplies = UserProfileControllerService.getUserReplies(targetUser.getId());
        targetUserPostHistory.setViewUserForumReplies(targetForumReplies);
        /** Events need username, userid, or UserEntity inside model **/
        List<Event> targetEventPost = UserProfileControllerService.getUserEventPost(targetUser.getUserName());
        targetUserPostHistory.setViewUserEventPost(targetEventPost);
        /** Services, Resources, RestaurantReview need username, userid, or UserEntity inside model **/

        return new ResponseEntity<>(new UserBundle(targetUser, targetInformation, targetDirectMessages, targetUserPostHistory), HttpStatus.OK);
    }

    @GetMapping("/getHiddenPostList/{userId}")
    public List<HiddenPost> getHiddenPostListByUserId(@PathVariable int userId) {
        List<HiddenPost> hiddenPostList = new ArrayList<>();
        for (HiddenPost post : hiddenPostRepository.findAll()) {
            if (post.getUserId() == userId) {
                hiddenPostList.add(post);
            }
        }
        return hiddenPostList;
    }

    @GetMapping("/getUpdatedPostHistoryViewUser/{userId}")
    public ResponseEntity<?> getUpdatedPostHistoryViewUser(@PathVariable int userId) {

        if (!userRepository.existsById(userId)) {
            return new ResponseEntity<>(new ResponseMessage("404"), HttpStatus.OK);
        }

        List<ForumPosts> currentPostSettings = new ArrayList<>();
        List<HiddenPost> hiddenPostList = new ArrayList<>();
        List<ForumPosts> userPost = new ArrayList<>();

        for (HiddenPost post : hiddenPostRepository.findAll()) {
            if (post.getUserId() == userId) {
                hiddenPostList.add(post);
            }
        }

        for (ForumPosts post : forumPostRepository.findAll()) {
            if (post.getUserEntity().getId() == userId) {
                userPost.add(post);
                currentPostSettings.add(post);
            }
        }

        for (ForumPosts post : userPost) {
            for (HiddenPost hidden : hiddenPostList) {
                if (post.getId() == hidden.getHidePostId()) {
                    currentPostSettings.remove(post);
                }
            }
        }

        return new ResponseEntity<>(currentPostSettings, HttpStatus.OK);
    }

    @GetMapping(path = {"/userProfileImage/{userId}"})
    public ResponseEntity<?> getImageDetails(@PathVariable("userId") String userId) throws IOException {

        final Optional<ProfilePicture> dbImage = profilePictureRepository.findByUserId(userId);
        if (dbImage.isEmpty()) {
            ResponseMessage responseMessage = new ResponseMessage("User does not have a profile picture.");
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        }

        return new ResponseEntity<>(ProfilePicture.builder()
                .id(dbImage.get().getId())
                .userId(dbImage.get().getUserId())
                .type(dbImage.get().getType())
                .image(ImageUtility.decompressImage(dbImage.get().getImage())).build(), HttpStatus.OK);
    }


    /** POST REQUEST **/
    /** POST REQUEST **/
    /** POST REQUEST **/

    @PostMapping("/upload/image")
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile file)
            throws IOException {

        if (profilePictureRepository.findByUserId(file.getOriginalFilename()).isPresent()) {
            Optional<ProfilePicture> remove = profilePictureRepository.findByUserId(file.getOriginalFilename());
            profilePictureRepository.deleteById(remove.get().getId());
        }

        profilePictureRepository.save(ProfilePicture.builder()
                .userId(file.getOriginalFilename())
                .type(file.getContentType())
                .image(ImageUtility.compressImage(file.getBytes())).build());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseMessage("Image uploaded successfully: " + file.getOriginalFilename()));
    }

    @PostMapping("/hidePostList")
    public ResponseEntity<?> hiddenPosts(@RequestBody HidePostDTO hidePostDTO) {

        /** hide post based on their postType, sort through repos to find the right object to hide **/

            for (HiddenPost post : hiddenPostRepository.findAll()) {
                if (Objects.equals(hidePostDTO.getPostType(), post.getPostType()) && Objects.equals(post.getHidePostId(), hidePostDTO.getHidePostId()) && Objects.equals(hidePostDTO.getUserId(), post.getUserId())) {
                    ResponseMessage responseMessage = new ResponseMessage("Post Already Hidden");
                    return new ResponseEntity<>(responseMessage, HttpStatus.OK);
                }
            }

        HiddenPost hiddenPost = new HiddenPost(hidePostDTO.getPostType(), hidePostDTO.getHidePostId(), hidePostDTO.getUserId());
        hiddenPostRepository.save(hiddenPost);

        return new ResponseEntity<>(hiddenPostRepository.findAll(), HttpStatus.OK);
    }

    @PostMapping("/unHidePost")
    public ResponseEntity<?> unHidePostFromProfile(@RequestBody HidePostDTO hidePostDTO) {

        for (HiddenPost post : hiddenPostRepository.findAll()) {
            if (Objects.equals(hidePostDTO.getHidePostId(), post.getHidePostId()) && Objects.equals(hidePostDTO.getPostType(), post.getPostType()) && Objects.equals(hidePostDTO.getUserId(), post.getUserId())) {
                hiddenPostRepository.delete(post);
                ResponseMessage responseMessage = new ResponseMessage("Post is no longer hidden.");
                return new ResponseEntity<>(responseMessage, HttpStatus.OK);
            }
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/sendDirectMessage")
    public ResponseEntity<?> updateUserInformation(@RequestBody DirectMessageDTO directMessageDTO) {

        DirectMessage messageSent = new DirectMessage(directMessageDTO.getReceivedByUserId(),
                                                      directMessageDTO.getReceivedByUserName(),
                                                      directMessageDTO.getSentByUserId(),
                                                      directMessageDTO.getSentByUserName(),
                                                      directMessageDTO.getMessageContent());

        if(messageSent.getMessageContent().toCharArray().length < 3) {
            throw new MinimumCharacterException();
        }

        directMessageRepository.save(messageSent);

        UserDmHistory targetDirectMessages = UserProfileControllerService.activeUserDirectMessageHistory(directMessageDTO.getSentByUserId());
        List<DirectMessage> allDirectMessageHistory = targetDirectMessages.getDirectMessageList();



        return new ResponseEntity<>(allDirectMessageHistory, HttpStatus.OK);

    }

    /** PUT Request **/
    /** PUT Request **/
    /** PUT Request **/


    @PutMapping("/update-user-information/{userId}")
    public ResponseEntity<?> updateUserInformation(@PathVariable int userId, @RequestBody UserInfoDTO userInfoDTO) {

        Optional<UserInformation> userInfo = userInformationRepository.findByUserId(userId);

        userInfo.get().setFirstName(userInfoDTO.getFirstName());
        userInfo.get().setLastName(userInfoDTO.getLastName());
        userInfo.get().setNeighborhood(userInfoDTO.getNeighborhood());
        userInfo.get().setCity(userInfoDTO.getCity());
        userInfo.get().setState(userInfoDTO.getState());

        userInformationRepository.save(userInfo.get());

        return new ResponseEntity<>(userInfo, HttpStatus.OK);

    }

}
