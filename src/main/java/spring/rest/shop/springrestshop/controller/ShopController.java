package spring.rest.shop.springrestshop.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import spring.rest.shop.springrestshop.aspect.SecurityContext;
import spring.rest.shop.springrestshop.entity.Organization;
import spring.rest.shop.springrestshop.entity.User;
import spring.rest.shop.springrestshop.exception.EntityNotFoundException;
import spring.rest.shop.springrestshop.service.ProductService;
import spring.rest.shop.springrestshop.service.ShopService;
import spring.rest.shop.springrestshop.service.UserService;

import java.util.List;

@Controller
@Slf4j
public class ShopController {
    @Autowired
    UserService userService;
    @Autowired
    ShopService shopService;
    @Autowired
    ProductService productService;


    @GetMapping("/shop")
    public String pageMyShop(Model model) {
        List<Organization> listActiveShopForCurrentUser = shopService.
                getListActivityShopForCurrentUser();
        List<Organization> listModerationShopForCurrentUser = shopService.getListModerationShopForCurrentUser();

        model.addAttribute("listActiveShopForCurrentUser", listActiveShopForCurrentUser);
        model.addAttribute("listModerationShopForCurrentUser", listModerationShopForCurrentUser);
        return "shop/shop-page";
    }

    @GetMapping("/addShop")
    public String addShopPage(Model model) {
        model.addAttribute("shopForm", new Organization());
        return "shop/add-shop";
    }

    @PostMapping("/addShop")
    public String addShop(@ModelAttribute("shopForm") @Validated Organization shopForm,
                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "shop/add-shop";
        }
        shopService.saveShop(shopForm);


        return "redirect:/shop";
    }


    @GetMapping("/editShop")
    public String editShop(@RequestParam("shopId") int shopId, Model model) {
        User currentUser = SecurityContext.getCurrentUser();
        if (shopService.getShopDetails(shopId).getOwner() == currentUser
                || currentUser.getRoles().stream().anyMatch(role -> role.name().equals("ROLE_ADMIN"))) {
            Organization currentShop = shopService.getShopDetails(shopId);
            model.addAttribute("shopForm", currentShop);
        }

        return "shop/add-shop";
    }

    @PostMapping("/deleteShop")
    public String deleteShop(@RequestParam("shopId") int shopId, Model model, Authentication authentication) throws EntityNotFoundException {
        User currentUser = userService.findUserByUsername(authentication.getName());
        if (shopService.getShopDetails(shopId).getOwner() == currentUser
                || currentUser.getRoles().stream().anyMatch(role -> role.name().equals("ROLE_ADMIN"))) {
            shopService.deleteShop(shopId);
        }

        return "redirect:/shop";
    }

    @GetMapping("/viewShop")
    public String viewShop(@RequestParam("shopId") int id, Model model) {
        model.addAttribute("currentShop", shopService.getShopDetails(id));
        return "shop/details";

    }
}
