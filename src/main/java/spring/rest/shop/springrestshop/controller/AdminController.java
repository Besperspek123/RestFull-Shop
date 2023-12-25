package spring.rest.shop.springrestshop.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import spring.rest.shop.springrestshop.dto.notification.NotificationDTO;
import spring.rest.shop.springrestshop.entity.Notification;
import spring.rest.shop.springrestshop.entity.Order;
import spring.rest.shop.springrestshop.entity.Organization;
import spring.rest.shop.springrestshop.entity.User;
import spring.rest.shop.springrestshop.exception.EmptyFieldException;
import spring.rest.shop.springrestshop.exception.EntityNotFoundException;
import spring.rest.shop.springrestshop.exception.UserAlreadyBannedException;
import spring.rest.shop.springrestshop.exception.UserNotBannedException;
import spring.rest.shop.springrestshop.service.NotificationService;
import spring.rest.shop.springrestshop.service.ShopService;
import spring.rest.shop.springrestshop.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {
    private final UserService userService;
    private final ShopService shopService;
    private final NotificationService notificationService;

    @GetMapping("/panel")
    public String userList(Model model) {
        model.addAttribute("allUsers", userService.getAllUsers());
        return "redirect:/admin/users";
    }

    @GetMapping("/users")
    public String getUsersInAdminPanel(Model model){
      List<User> allUsers = userService.getAllUsers();
        model.addAttribute("allUsers",allUsers);
        return "admin/users";
    }
    @GetMapping("/shops")
    public String getShopsInAdminPanel(Model model){
        List<Organization> allShops = shopService.getAllShops();
        model.addAttribute("allShops",allShops);
        return "admin/shops";
    }
    @GetMapping("/searchUser")
    public String searchUser(@RequestParam(name = "searchQuery") String searchQuery,Model model){
        List<User> users = userService.findUsersByUsernameContaining(searchQuery);
        model.addAttribute("allUsers",users);
        return "admin/users";
    }
    @PostMapping("/editUser")
    public String editUser (@ModelAttribute("userForm") @Validated User userForm, Model model, RedirectAttributes redirectAttributes){
        if(!userForm.getPassword().equals(userForm.getPasswordConfirm())){
            model.addAttribute("passwordError", "Пароли не совпадают");
            return "admin/edit-user";
        }
        userService.editUser(userForm);
        return "redirect:/admin/users";
    }
    @GetMapping("/userInfo")
    public String userInfo(@RequestParam(name = "userId")long userId,Model model){
        User user = userService.getUserById(userId);
        model.addAttribute("user",user);
        return "admin/user-info";
    }
    @GetMapping("/shopInfo")
    public String shopInfo(@RequestParam(name = "shopId")long shopId,Model model){
        Organization shop = shopService.getShopDetails((int)shopId);
        model.addAttribute("currentShop",shop);
        return "shop/details";
    }
    @GetMapping("/searchShop")
    public String searchShop(@RequestParam(name = "searchQuery") String searchQuery,Model model){
        List<Organization> shops = shopService.getShopsByNameContaining(searchQuery);
        model.addAttribute("allShops",shops);
        return "admin/shops";
    }
    @PostMapping("/banUser")
    public String banUser(@RequestParam(name = "userId")long userId){
        User userForBan = userService.getUserById(userId);
        userService.banUser(userForBan);
        return "redirect:/admin/userInfo?userId=" + userId;
    }
    @PostMapping("/unbanUser")
    public String unbanUser(@RequestParam(name = "userId")long userId){
        User userForUnban = userService.getUserById(userId);
        userService.unbanUser(userForUnban);
        return "redirect:/admin/userInfo?userId=" + userId;
    }

    @GetMapping("/editUser")
    public String editUser(@RequestParam(name = "userId") long userId,Model model,Authentication authentication){
        User userForm = userService.getUserById(userId);
        model.addAttribute("userForm",userForm);
        return "admin/edit-user";
    }

    @GetMapping("/addBalance")
    public String addBalance(@RequestParam(name = "userId") long userId,Model model){
        User user = userService.getUserById(userId);
        model.addAttribute("user",user);
        return "admin/add-balance";
    }
    @PostMapping("/addBalance")
    public String addBalance(@RequestParam(name = "deposit")int deposit,@RequestParam(name = "userId")long userId){
        userService.addBalance(userId,deposit);
        return "redirect:/admin/userInfo?userId=" + userId;
    }

    @GetMapping("/checkOrders")
    public String checkUserOrders(@RequestParam(name = "userId") long userId, Authentication authentication, Model model){
        User currentUser = userService.findUserByUsername(authentication.getName());
        model.addAttribute("currentUser",currentUser);
        List<Order> orderList = userService.getUserById(userId).getOrderList();
        model.addAttribute("ordersList",orderList);

        return "order/orders-page";
    }
    @GetMapping("/moderation")
    public String moderationShopPage(Model model, Authentication authentication){
        User currentUser = userService.findUserByUsername(authentication.getName());
        model.addAttribute("currentUser",currentUser);
        List<Organization> allShops = shopService.getAllModerationShops();
        model.addAttribute("allShops",allShops);
        return "admin/moderation";
    }
    @GetMapping("/searchModerationShop")
    public String searchModerationShop(@RequestParam(name = "searchQuery") String searchQuery,Model model,Authentication authentication){
        User currentUser = userService.findUserByUsername(authentication.getName());
        model.addAttribute("currentUser",currentUser);
        List<Organization> shops = shopService.getAllModerationShopsByNameContaining(searchQuery);
        model.addAttribute("allShops",shops);
        return "admin/moderation";
    }
    @PostMapping("/approveShop")
    public String approveShop(@RequestParam(name = "shopId") long shopId){
        shopService.approveShop(shopId);

        return "redirect:/admin/moderation";
    }

    @PostMapping("/disapproveAllShops")
    public String disapproveAllShops(Authentication authentication) throws EntityNotFoundException {
        shopService.disapproveAllShops(userService.findUserByUsername(authentication.getName()));
        return "redirect:/admin/moderation";
    }

    @PostMapping("/approveAllShops")
    public String approveAllShops(){
        shopService.approveAllShops();
        return "redirect:/admin/moderation";
    }
    @PostMapping("/deleteShop")
    public String deleteShop(@RequestParam(name = "shopId")long shopId) throws EntityNotFoundException {
        shopService.deleteShop(shopId);
        return "redirect:/admin/shops";
    }

    @GetMapping("/viewShopsUser")
    public String viewShopsUser(@RequestParam(name = "userId") long userId, Model model){
        List<Organization> userShopList = shopService.getListActivityShopForUser(userService.getUserById(userId));
        model.addAttribute("allShops",userShopList);
        return "admin/shops";
    }


    @GetMapping("/sendNotification")
    public String sendNotification(@RequestParam(name = "userId") long userId, Model model){
        model.addAttribute("notificationForm", new Notification());
        model.addAttribute("userId", userId);
        return "notification/send-notification-page";
    }

    @PostMapping("/sendNotification")
    public String sendNotification(@RequestParam(name = "userId") long userId, @Validated NotificationDTO notificationForm) throws EntityNotFoundException, EmptyFieldException {
        notificationService.sendMessage(userId,notificationForm);
        return "redirect:/admin/userInfo?userId=" + userId;
    }
}

