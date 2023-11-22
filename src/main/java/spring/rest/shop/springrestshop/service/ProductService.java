package spring.rest.shop.springrestshop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import spring.rest.shop.springrestshop.aspect.SecurityContext;
import spring.rest.shop.springrestshop.entity.*;
import spring.rest.shop.springrestshop.exception.EntityNotFoundException;
import spring.rest.shop.springrestshop.exception.UnauthorizedShopAccessException;
import spring.rest.shop.springrestshop.repository.*;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private ShopService shopService;

    @Autowired
    public void setShopService(@Lazy ShopService shopService) {
        this.shopService = shopService;
    }
    private final KeywordRepository keywordRepository;
    private final CharacteristicRepository characteristicRepository;
    private final CartProductRepository cartProductRepository;


    public List<Product> getAvailableProductsList(){
        return productRepository.findAllByOrganization_ActivityTrue();
    }

    public List<Product> getProductsFromShop(long shopId) throws EntityNotFoundException {
        User currentUser =SecurityContext.getCurrentUser();

        if(shopService.getShopById(shopId) == null){
            throw new EntityNotFoundException("Shop with ID " + shopId + " not found");
        }
        if(currentUser != shopService.getShopById(shopId).getOwner()
                && currentUser.getRoles().stream().noneMatch(role -> role.name().equals("ROLE_ADMIN"))){
            throw new AccessDeniedException("You try get products from shop which doesn`t belong to you");
        }
        return productRepository.findByOrganization_Id(shopId);
    }

    public Product getProductDetails(long id) throws EntityNotFoundException {
        Product product = productRepository.getById(id);
        if(product == null){
            throw new EntityNotFoundException("Product with ID: " + id + " not found");
        }
        return product;

    }

    public boolean checkPermissionToAddProductIntoTheShop(long shopId){
        User currentUser = SecurityContext.getCurrentUser();
            if(currentUser.getRoles().stream().anyMatch(role -> role.name().equals("ROLE_ADMIN"))
                    || currentUser == shopService.getShopById(shopId).getOwner()){
                return true;
            }
            return false;

    }

    public void addProduct(Product product, long shopId) throws UnauthorizedShopAccessException, EntityNotFoundException {
        User currentUser = SecurityContext.getCurrentUser();
        System.out.println(currentUser.getUsername());
        Organization shop = shopService.getShopById(shopId);

        if (shop == null ){
            throw new EntityNotFoundException("Shop with ID: " + shopId + " not found");
        }

        if(!shop.getOwner().equals(currentUser)
        && currentUser.getRoles().stream().noneMatch(role -> role.name().equals("ROLE_ADMIN"))){
            throw new UnauthorizedShopAccessException("You are trying add product in not your shop");
        }
//        product.setOrganization(shopService.getShopById(shopId));
        product.setOrganization(shopService.getShopById(shopId));

        List<Characteristic> characteristicList = new ArrayList<>();
        String[] splitCharacteristicString = product.getCharacteristicsString().split(";");
        for (int i = 0; i < splitCharacteristicString.length; i++) {
            Characteristic characteristic = new Characteristic(splitCharacteristicString[i]);
            characteristicRepository.save(characteristic);
            characteristicList.add(characteristic);
        }

        product.setCharacteristicList(characteristicList);

        List<Keyword> keywordsList = new ArrayList<>();
        String[] splitKeywordsString = product.getKeywordsString().split(";");
        for (int i = 0; i < splitKeywordsString.length; i++) {
            Keyword keyword = new Keyword(splitKeywordsString[i]);
            keywordRepository.save(keyword);
            keywordsList.add(keyword);
        }

        product.setKeywordsList(keywordsList);
        System.out.println("Размер списка отзывов при создании товара: " +
                        product.getReviewsList().size());

//        if(product.getSale() != null){
//            product.setPrice(product.getPrice() - (product.getPrice() * (product.getSale() / 100)));
//        }
        product.setSale(0);
        productRepository.save(product);
        shopService.getShopById(shopId).getProductList().add(product);
    }

    public void editProduct(long productId, Product productForSave) throws EntityNotFoundException {
        Product product = productRepository.getById(productId);
        User currentUser = SecurityContext.getCurrentUser();
        if(product == null){
            throw new EntityNotFoundException("Product with ID " + productId + " not found" );
        }
        if(currentUser != product.getOrganization().getOwner()
                && currentUser.getRoles().stream().noneMatch(role -> role.name().equals("ROLE_ADMIN")) ){
            throw new AccessDeniedException("You try edit product which doesn`t  belong to you");
        }
        if(productForSave.getName() != null){
            product.setName(productForSave.getName());
        }
        if(productForSave.getCharacteristicsString() !=null){
            product.setCharacteristicsString(productForSave.getCharacteristicsString());
//            List<Characteristic> characteristicList = new ArrayList<>();
//            String[] splitCharacteristicString = product.getCharacteristicsString().split(";");
//            for (int i = 0; i < splitCharacteristicString.length; i++) {
//                Characteristic characteristic = new Characteristic(splitCharacteristicString[i]);
//                characteristicRepository.save(characteristic);
//                characteristicList.add(characteristic);
//            }
        }
        if(productForSave.getKeywordsString() != null){
            product.setKeywordsString(productForSave.getKeywordsString());
        }
        if (productForSave.getAmountInStore() != null){
            product.setAmountInStore(productForSave.getAmountInStore());
        }
        if(productForSave.getPrice() != null){
            product.setPrice(productForSave.getPrice());
        }
        if(productForSave.getSale() != null){
            product.setSale(productForSave.getSale());
            product.setPrice(product.getPrice() - (product.getPrice() * (product.getSale() / 100)));
        }
        if(productForSave.getDescription() != null){
            product.setDescription(productForSave.getDescription());
        }

        productRepository.save(product);


    }

    public void deleteProductInShop(long productId){
        User currentUser = SecurityContext.getCurrentUser();
        Product product = productRepository.getById(productId);
        if(product.getOrganization().getOwner() != currentUser
                || currentUser.getRoles().stream().noneMatch(role -> role.name().equals("ROLE_ADMIN"))){
            throw new AccessDeniedException("You dont have permission to delete this shop");
        }
        product.setOrganization(null);
        productRepository.save(product);
    }
    public List<Product> findProductByName(String name){

        productRepository.findAllByNameContainingIgnoreCase(name);
        List<Product> allProductsByNameContaining =  productRepository.findAllByNameContainingIgnoreCase(name);
        List<Product> allProductByNameContainingAndWhereShopActivityTrue = new ArrayList<>();
        for (Product product:allProductsByNameContaining
        ) {
            if(product.getOrganization()!=null){
                if(product.getOrganization().isActivity()){
                    allProductByNameContainingAndWhereShopActivityTrue.add(product);
                }
            }
        }
        return allProductByNameContainingAndWhereShopActivityTrue;

    }

    public void save(Product product) {
        productRepository.save(product);
    }


}

