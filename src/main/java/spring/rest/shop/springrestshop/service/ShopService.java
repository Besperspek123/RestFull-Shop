package spring.rest.shop.springrestshop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spring.rest.shop.springrestshop.aspect.SecurityContext;
import spring.rest.shop.springrestshop.dto.shop.ShopEditDTO;
import spring.rest.shop.springrestshop.entity.*;
import spring.rest.shop.springrestshop.exception.EmptyFieldException;
import spring.rest.shop.springrestshop.exception.EntityNotFoundException;
import spring.rest.shop.springrestshop.exception.UserNotFoundException;
import spring.rest.shop.springrestshop.repository.*;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopService {
    private final ShopRepository shopRepository;
    private final ProductService productService;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final UserRepository userRepository;
    private final CartProductRepository cartProductRepository;



    public List<Organization> getListActivityShopForUser(User user){
        if(user == null){
            throw new UserNotFoundException("User cant be null");
        }
        return shopRepository.getAllByOwnerAndActivityTrue(user);
    }
    public List<Organization> getListActivityShopForCurrentUser(){
        User currentUser = SecurityContext.getCurrentUser();
        return shopRepository.getAllByOwnerAndActivityTrue(currentUser);
    }
    public List<Organization> getListModerationShopForCurrentUser(){
        User currentUser = SecurityContext.getCurrentUser();
        return shopRepository.getAllByOwnerAndActivityFalse(currentUser);
    }

//    public void saveShop(Organization shop){
//        if (shop.getName()!= null) {
//            if (shop.getId() == 0) {
//                shop.setOwner(SecurityContext.getCurrentUser());
//            } else {
//                shop.setOwner(shopRepository.getOrganizationById(shop.getId()).getOwner());
//            }
//
//        if(!shop.getOwner().getRoles().contains(Role.ROLE_ADMIN)){
//            shop.setActivity(false);
//        }
//         else {shop.setActivity(true);}
//         shopRepository.save(shop);
//        }
//    }

    public void saveShop(Organization shop){
        if (shop.getName() == null || shop.getName().isEmpty()){
            throw new EmptyFieldException("Shop name cant be empty");
        }
        User currentUser =SecurityContext.getCurrentUser();
        if (shop.getId() == 0){
            shop.setOwner(currentUser);
        }
        else {
            shop.setOwner(shopRepository.getOrganizationById(shop.getId()).getOwner());
        }
        if(!shop.getOwner().getRoles().contains(Role.ROLE_ADMIN)){
            shop.setActivity(false);
        }
        else {shop.setActivity(true);}
        shopRepository.save(shop);


    }

    public void editShop(long shopId, ShopEditDTO shopForEdit) throws EntityNotFoundException {

        if(shopRepository.getOrganizationById(shopId) == null) {
            throw new EntityNotFoundException("Shop with ID: " + shopId);
        }
        Organization shop = shopRepository.getOrganizationById(shopId);
        if(shopForEdit.getName() != null){
            shop.setName(shopForEdit.getName());
        }
        if(shopForEdit.getDescription() != null){
            shop.setDescription(shopForEdit.getDescription());
        }
        shopRepository.save(shop);
    }

    public Organization getShopDetails(long id){
        return shopRepository.getOrganizationById(id);
    }

    public List<Organization> getAllShops(){
        return shopRepository.getAllByActivityIsTrue();
    }

    public List<Organization> getShopsByNameContaining(String string){
        return shopRepository.getAllByNameContainingAndActivityIsTrue(string);
    }

    public void deleteShop(long shopId) throws EntityNotFoundException {
        User currentUser = SecurityContext.getCurrentUser();
        if(shopRepository.getOrganizationById(shopId).getOwner() == currentUser
                || currentUser.getRoles().contains(Role.ROLE_ADMIN)){
            List<Product> productListForDeletedShop = productService.getProductsFromShop(shopId);
            for (Product product:productListForDeletedShop
            ) {
                product.setOrganization(null);
                productService.save(product);
            }
            List<Cart> cartThatContainCartProductWhereOrganizationNull = cartRepository.getAllBy();
            for (Cart cart:cartThatContainCartProductWhereOrganizationNull
                 ) {
                List<CartProduct> currentListCartProductInThisCart = cart.getCartProducts();
                List<CartProduct> newListCartProductInThisCart = new ArrayList<>();
                for (CartProduct cartProduct:currentListCartProductInThisCart
                     ) {
                    if(cartProduct.getProduct().getOrganization() != null){
                        newListCartProductInThisCart.add(cartProduct);
                    }
                    else {
                        cartProduct.setCart(null);
                        cartProductRepository.save(cartProduct);
                    }
                    }
                    cart.setCartProducts(newListCartProductInThisCart);
                    cartRepository.save(cart);
                    cartService.calculateTotalCost(cart);
                }
            shopRepository.deleteById(shopId);
            }



        }

    public List<Organization> getAllModerationShops(){
        return shopRepository.getAllByActivityIsFalse();
    }


    public void approveShop(long shopId) {
        Organization shop = shopRepository.getOrganizationById((int)shopId);
        shop.setActivity(true);
        shopRepository.save(shop);
    }

    public List<Organization> getAllModerationShopsByNameContaining(String searchQuery) {
       return shopRepository.getAllByNameContainingAndActivityIsFalse(searchQuery);
    }

    public void approveAllShops(){
        List<Organization> shopList = shopRepository.getAllByActivityIsFalse();
        for (Organization shop:shopList
             ) {
            shop.setActivity(true);
            shopRepository.save(shop);
        }
    }
    public void disapproveAllShops(User currentUser) throws EntityNotFoundException {
        List<Organization> shopList = shopRepository.getAllByActivityIsFalse();
        for (Organization shop:shopList
        ) {
            deleteShop(shop.getId());

        }
    }
    public Organization getShopById(long shopId){
        return shopRepository.getOrganizationById(shopId);
    }
}

