package spring.rest.shop.springrestshop.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import spring.rest.shop.springrestshop.aspect.SecurityContext;
import spring.rest.shop.springrestshop.entity.Product;
import spring.rest.shop.springrestshop.entity.User;
import spring.rest.shop.springrestshop.exception.EntityNotFoundException;
import spring.rest.shop.springrestshop.exception.PermissionForSaveThisProductDeniedException;
import spring.rest.shop.springrestshop.exception.UnauthorizedShopAccessException;
import spring.rest.shop.springrestshop.repository.ProductRepository;
import spring.rest.shop.springrestshop.service.ProductService;
import spring.rest.shop.springrestshop.service.ShopService;
import spring.rest.shop.springrestshop.service.UserService;

import java.util.List;

@Controller
@Slf4j
public class ProductController {
    @Autowired
    UserService userService;
    @Autowired
    ShopService shopService;
    @Autowired
    ProductService productService;

    @Autowired
    ProductRepository productRepository;


        @GetMapping("/addProduct")
        public String addProduct(@RequestParam("shopId") int shopId,Model model){
            model.addAttribute("productForm",new Product());
            model.addAttribute("shopId",shopId);
            return "product/add-product";
        }
        //TODO пересмотреть валидация и отправить её в сервис
        @PostMapping("/addProduct")
        public String addProductForCurrentShop(@ModelAttribute("productForm") @Validated Product productForm,
        @RequestParam ("shopId") int shopId) throws UnauthorizedShopAccessException, EntityNotFoundException {
            if(productService.checkPermissionToAddProductIntoTheShop(shopId)){
                productService.addProduct(productForm,shopId);
            }
            else throw new PermissionForSaveThisProductDeniedException("You don`t have permission to save this product to this shop");
        return "redirect:/viewShop?shopId="+ shopId;
        }

        @GetMapping("/editProduct")
        public String editProduct(@RequestParam("shopId") int shopId,@RequestParam("productId") int productId, Model model) throws EntityNotFoundException {
            model.addAttribute("productForm", productService.getProductDetails(productId));
            model.addAttribute("shopId",shopId);
            return "product/add-product";
        }

        @PostMapping("/deleteProduct")
        public String deleteProduct(@RequestParam("productId") long productId) {
            Product product = productService.getProductDetails(productId);
            long shopId = product.getOrganization().getId();
                productService.deleteProductInShop(productId);
            return "redirect:/viewShop?shopId=" + shopId;
        }


        @GetMapping("/viewProduct")
        public String viewProduct(@RequestParam("productId") int productId, Model model) throws EntityNotFoundException {
        model.addAttribute(productService.getProductDetails(productId));
        return "product/details";
        }
        @GetMapping("/searchProducts")
        public String searchProducts(@RequestParam("searchQuery") String searchQuery,Model model){
            List<Product> productList = productService.findProductByName(searchQuery);
            model.addAttribute("productList",productList);
            return "main";
        }


}
