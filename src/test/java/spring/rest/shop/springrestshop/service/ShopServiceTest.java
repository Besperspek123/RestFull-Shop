package spring.rest.shop.springrestshop.service;

import org.aspectj.weaver.ast.Or;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import spring.rest.shop.springrestshop.aspect.SecurityContext;
import spring.rest.shop.springrestshop.dto.shop.ShopEditDTO;
import spring.rest.shop.springrestshop.entity.Organization;
import spring.rest.shop.springrestshop.entity.Role;
import spring.rest.shop.springrestshop.entity.User;
import spring.rest.shop.springrestshop.exception.EmptyFieldException;
import spring.rest.shop.springrestshop.exception.EntityNotFoundException;
import spring.rest.shop.springrestshop.exception.UserNotFoundException;
import spring.rest.shop.springrestshop.repository.ShopRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    ShopRepository shopRepository;

    @InjectMocks
    ShopService shopService;

    @Test
    void TryGetActivityShopOfUser_ShouldReturnListOfShops(){
        User currentUser = new User(1L, "user", true, "password", "password", "email");
        List<Organization> organizationList = currentUser.getOrganizationList();
        organizationList.add(new Organization("shop1"));
        organizationList.add(new Organization("shop2"));
        organizationList.add(new Organization("shop3"));
        organizationList.get(0).setActivity(true);
        organizationList.get(1).setActivity(true);
        organizationList.get(2).setActivity(true);

            when(shopRepository.getAllByOwnerAndActivityTrue(currentUser)).thenReturn(currentUser.getOrganizationList());
            List<Organization> actualListOfShops = shopService.getListActivityShopForUser(currentUser);
            assertEquals(actualListOfShops.get(0).getName(),"shop1");
            assertEquals(actualListOfShops.get(1).getName(),"shop2");
            assertEquals(actualListOfShops.get(2).getName(),"shop3");
        assertEquals(actualListOfShops.get(0).isActivity(),true);
        assertEquals(actualListOfShops.get(1).isActivity(),true);
        assertEquals(actualListOfShops.get(2).isActivity(),true);
            verify(shopRepository).getAllByOwnerAndActivityTrue(currentUser);

    }
    @Test
    void TryGetActivityShopOfUser_ShouldReturnEmptyList(){
        User user = new User(1L, "user", true, "password", "password", "email");
            when(shopRepository.getAllByOwnerAndActivityTrue(user)).thenReturn(user.getOrganizationList());
            List<Organization> actualListOfShops = shopService.getListActivityShopForUser(user);
            assertEquals(actualListOfShops.size(),0);
            verify(shopRepository).getAllByOwnerAndActivityTrue(user);

    }
    @Test
    void TryToGetActivityShopForUserThatIsNull_ShouldThrowException(){
        User user = null;
        assertThrows(NullPointerException.class,() -> shopService.getListActivityShopForUser(user));

    }
    @Test
    void TryGetActivityShopOfUserThatIdIsNull_ShouldThrowException(){
        User user = new User(null, "user", true, "password", "password", "email");
        assertThrows(NullPointerException.class,() -> shopService.getListActivityShopForUser(user));

    }

    @Test
    void TryToGetActivityShopForCurrentUser_ShouldReturnList(){
        User currentUser = new User(1L, "admin", true, "password", "password", "email");
        List<Organization> organizationList = currentUser.getOrganizationList();
        organizationList.add(new Organization("shop1"));
        organizationList.add(new Organization("shop2"));
        organizationList.add(new Organization("shop3"));
        organizationList.get(0).setActivity(true);
        organizationList.get(1).setActivity(false);
        organizationList.get(2).setActivity(true);
        organizationList.get(0).setOwner(currentUser);
        organizationList.get(1).setOwner(currentUser);
        organizationList.get(2).setOwner(currentUser);
        List<Organization> listWithActivityShop = new ArrayList<>();
        listWithActivityShop.add(organizationList.get(0));
        listWithActivityShop.add(organizationList.get(1));try (MockedStatic<SecurityContext> mocked = mockStatic(SecurityContext.class)) {
            mocked.when(SecurityContext::getCurrentUser).thenReturn(currentUser);
            when(shopRepository.getAllByOwnerAndActivityTrue(currentUser)).thenReturn(listWithActivityShop);
            assertEquals(listWithActivityShop,shopService.getListActivityShopForCurrentUser());
            verify(shopRepository).getAllByOwnerAndActivityTrue(currentUser);
        }
    }

    @Test
    void TryToGetActivityShopForCurrentUser_ShouldReturnEmptyList(){
        User currentUser = new User(1L, "admin", true, "password", "password", "email");
        try (MockedStatic<SecurityContext> mocked = mockStatic(SecurityContext.class)) {
            mocked.when(SecurityContext::getCurrentUser).thenReturn(currentUser);
            when(shopRepository.getAllByOwnerAndActivityTrue(currentUser)).thenReturn(currentUser.getOrganizationList());
            List<Organization> actualList = shopService.getListActivityShopForCurrentUser();
            assertTrue(actualList.isEmpty());
            verify(shopRepository).getAllByOwnerAndActivityTrue(currentUser);
        }
    }
    @Test
    void TryToGetActivityShopForCurrentUserThatIsNull_ShouldThrowException(){
        User currentUser = null;
        try (MockedStatic<SecurityContext> mocked = mockStatic(SecurityContext.class)) {
            mocked.when(SecurityContext::getCurrentUser).thenReturn(currentUser);
            assertThrows(NullPointerException.class,() -> shopService.getListActivityShopForCurrentUser());

        }
    }
    @Test
    void TryToGetActivityShopForCurrentUserThatIdIsNull_ShouldThrowException(){
        User currentUser = new User(null, "admin", true, "password", "password", "email");
        try (MockedStatic<SecurityContext> mocked = mockStatic(SecurityContext.class)) {
            mocked.when(SecurityContext::getCurrentUser).thenReturn(currentUser);
            assertThrows(NullPointerException.class,() -> shopService.getListActivityShopForCurrentUser());

        }
    }
    @Test
    void getListModerationShopForCurrentUser_ShouldReturnListWithShopsThatActivityIsFalse(){
        User currentUser = new User(1L, "admin", true, "password", "password", "email");
        List<Organization> organizationList = currentUser.getOrganizationList();
        organizationList.add(new Organization("shop1"));
        organizationList.add(new Organization("shop2"));
        organizationList.add(new Organization("shop3"));
        organizationList.get(0).setActivity(true);
        organizationList.get(1).setActivity(false);
        organizationList.get(2).setActivity(true);
        organizationList.get(0).setOwner(currentUser);
        organizationList.get(1).setOwner(currentUser);
        organizationList.get(2).setOwner(currentUser);
        try (MockedStatic<SecurityContext> mocked = mockStatic(SecurityContext.class)) {
            mocked.when(SecurityContext::getCurrentUser).thenReturn(currentUser);
            List<Organization> excepctedList = new ArrayList<>();
            excepctedList.add(organizationList.get(1));
            when(shopRepository.getAllByOwnerAndActivityFalse(currentUser)).thenReturn(excepctedList);
            List<Organization> actialList = shopService.getListModerationShopForCurrentUser();
            assertEquals(actialList,excepctedList);
            assertFalse(actialList.get(0).isActivity());
            verify(shopRepository).getAllByOwnerAndActivityFalse(currentUser);
        }
    }
    @Test
    void getListModerationShopForCurrentUser_ShouldReturnEmptyList(){
        User currentUser = new User(1L, "admin", true, "password", "password", "email");
        List<Organization> organizationList = currentUser.getOrganizationList();
        organizationList.add(new Organization("shop1"));
        organizationList.add(new Organization("shop2"));
        organizationList.add(new Organization("shop3"));
        organizationList.get(0).setActivity(false);
        organizationList.get(1).setActivity(false);
        organizationList.get(2).setActivity(false);
        organizationList.get(0).setOwner(currentUser);
        organizationList.get(1).setOwner(currentUser);
        organizationList.get(2).setOwner(currentUser);
        try (MockedStatic<SecurityContext> mocked = mockStatic(SecurityContext.class)) {
            mocked.when(SecurityContext::getCurrentUser).thenReturn(currentUser);
            List<Organization> excepctedList = new ArrayList<>();
            when(shopRepository.getAllByOwnerAndActivityFalse(currentUser)).thenReturn(excepctedList);
            List<Organization> actialList = shopService.getListModerationShopForCurrentUser();
            assertEquals(actialList,excepctedList);
            assertEquals(actialList.size(),0);
            verify(shopRepository).getAllByOwnerAndActivityFalse(currentUser);
        }
    }
    @Test
    void getListModerationShopForCurrentUserThatIsNull_ShouldThrowException(){
        User currentUser = null;

        try (MockedStatic<SecurityContext> mocked = mockStatic(SecurityContext.class)) {
            mocked.when(SecurityContext::getCurrentUser).thenReturn(currentUser);
            assertThrows(NullPointerException.class,() -> shopService.getListModerationShopForCurrentUser());
        }
    }
    @Test
    void getListModerationShopForCurrentUserThatIdIsNull_ShouldThrowException(){
        User currentUser = new User(null, "admin", true, "password", "password", "email");
        try (MockedStatic<SecurityContext> mocked = mockStatic(SecurityContext.class)) {
            mocked.when(SecurityContext::getCurrentUser).thenReturn(currentUser);
            assertThrows(NullPointerException.class,() -> shopService.getListModerationShopForCurrentUser());
        }
    }

    @Test
    void saveShopThatNameIsEmpty_ShouldThrowException(){
        User currentUser = new User(1L, "admin", true, "password", "password", "email");
        Organization shop = new Organization();
        try (MockedStatic<SecurityContext> mocked = mockStatic(SecurityContext.class)) {
            mocked.when(SecurityContext::getCurrentUser).thenReturn(currentUser);
            assertThrows(EmptyFieldException.class, () ->shopService.saveShop(shop));
        }

    }
    @Test
    void saveShopThatNameIsNull_ShouldThrowException(){
        User currentUser = new User(1L, "admin", true, "password", "password", "email");
        Organization shop = new Organization();
        try (MockedStatic<SecurityContext> mocked = mockStatic(SecurityContext.class)) {
            mocked.when(SecurityContext::getCurrentUser).thenReturn(currentUser);
            shop.setName(null);
            assertThrows(EmptyFieldException.class, () ->shopService.saveShop(shop));

        }

    }
    @Test
    void CurrentUserThatIsNull_TryToSaveShop_ShouldThrowException() {
        User currentUser = null;
        Organization shop = new Organization();
        shop.setName("shop");
        shop.setDescription("description");
        try (MockedStatic<SecurityContext> mocked = mockStatic(SecurityContext.class)) {
            mocked.when(SecurityContext::getCurrentUser).thenReturn(currentUser);
            assertThrows(NullPointerException.class, () -> shopService.saveShop(shop));

        }
    }
    @Test
    void CurrentUserThatIdIsNull_TryToSaveShop_ShouldThrowException() {
        User currentUser = new User(null, "admin", true, "password", "password", "email");

        Organization shop = new Organization();
        shop.setName("shop");
        shop.setDescription("description");
        try (MockedStatic<SecurityContext> mocked = mockStatic(SecurityContext.class)) {
            mocked.when(SecurityContext::getCurrentUser).thenReturn(currentUser);
            assertThrows(NullPointerException.class, () -> shopService.saveShop(shop));

        }
    }
    @Test
    void saveShop_ShouldSaveShop(){
        User currentUser = new User(1L, "user", true, "password", "password", "email");
        Organization shop = new Organization();
        shop.setName("shop");
        shop.setDescription("description");
        try (MockedStatic<SecurityContext> mocked = mockStatic(SecurityContext.class)) {
            mocked.when(SecurityContext::getCurrentUser).thenReturn(currentUser);
            shopService.saveShop(shop);
            assertEquals(currentUser,shop.getOwner());
            assertFalse(shop.isActivity());
            assertFalse(shop.getName().isEmpty());
            verify(shopRepository).save(shop);
        }
    }
    @Test
    void CurrentUserTryToSaveShopThatIdIsNotNull_ShouldSaveShop(){
        User currentUser = new User(1L, "user", true, "password", "password", "email");
        Organization shop = new Organization();
        shop.setId(1L);
        shop.setName("shop");
        shop.setDescription("description");
        try (MockedStatic<SecurityContext> mocked = mockStatic(SecurityContext.class)) {
            mocked.when(SecurityContext::getCurrentUser).thenReturn(currentUser);
            shopService.saveShop(shop);
            assertEquals(currentUser,shop.getOwner());
            assertFalse(shop.isActivity());
            assertFalse(shop.getName().isEmpty());
            verify(shopRepository).save(shop);
        }
    }

    @Test
    void CurrentOwnerOfShopTryToEditShop_ShouldEditShop(){
        User currentUser = new User(1L, "user", true, "password", "password", "email");
        Organization shopOfUser = new Organization("shop");
        shopOfUser.setId(1L);
        shopOfUser.setOwner(currentUser);
        currentUser.getOrganizationList().add(shopOfUser);
        ShopEditDTO newShop = new ShopEditDTO();
        newShop.setName("newShop");
        newShop.setDescription("newDescription");
        try (MockedStatic<SecurityContext> mocked = mockStatic(SecurityContext.class)) {
            mocked.when(SecurityContext::getCurrentUser).thenReturn(currentUser);
            when(shopRepository.getOrganizationById(1L)).thenReturn(shopOfUser);
            shopService.editShop(1L,newShop);
            assertEquals(shopOfUser.getName(),"newShop");
            assertEquals(shopOfUser.getDescription(),"newDescription");
            assertEquals(shopOfUser.getOwner(),currentUser);
            verify(shopRepository).save(shopOfUser);
        }

    }

    @Test
    void AnotherUserTryToEditNotHisShop_ShouldThrowException(){
        User currentUser = new User(1L, "user", true, "password", "password", "email");
        Organization shopOfUser = new Organization("shop");
        shopOfUser.setId(1L);
        currentUser.getOrganizationList().add(shopOfUser);
        ShopEditDTO newShop = new ShopEditDTO();
        newShop.setName("newShop");
        newShop.setDescription("newDescription");
        try (MockedStatic<SecurityContext> mocked = mockStatic(SecurityContext.class)) {
            mocked.when(SecurityContext::getCurrentUser).thenReturn(currentUser);
            when(shopRepository.getOrganizationById(1L)).thenReturn(shopOfUser);
            assertThrows(AccessDeniedException.class,() -> shopService.editShop(1L,newShop));

        }

    }
    @Test
    void AdminTryToEditShop_ShouldEditShop(){
        User currentUser = new User(1L, "user", true, "password", "password", "email");
        currentUser.getRoles().add(Role.ROLE_ADMIN);
        Organization shopOfUser = new Organization("shop");
        shopOfUser.setId(1L);
        currentUser.getOrganizationList().add(shopOfUser);
        ShopEditDTO newShop = new ShopEditDTO();
        newShop.setName("newShop");
        newShop.setDescription("newDescription");
        try (MockedStatic<SecurityContext> mocked = mockStatic(SecurityContext.class)) {
            mocked.when(SecurityContext::getCurrentUser).thenReturn(currentUser);
            when(shopRepository.getOrganizationById(1L)).thenReturn(shopOfUser);
            shopService.editShop(1L,newShop);
            assertEquals(shopOfUser.getName(),"newShop");
            assertEquals(shopOfUser.getDescription(),"newDescription");
            verify(shopRepository).save(shopOfUser);

        }

    }

    @Test
    void UserTryToEditShopWithEmptyName_ShouldThrowException(){
        User currentUser = new User(1L, "user", true, "password", "password", "email");
        Organization shopOfUser = new Organization("shop");
        shopOfUser.setId(1L);
        shopOfUser.setOwner(currentUser);
        currentUser.getOrganizationList().add(shopOfUser);
        ShopEditDTO newShop = new ShopEditDTO();
        newShop.setName("");
        newShop.setDescription("newDescription");
        try (MockedStatic<SecurityContext> mocked = mockStatic(SecurityContext.class)) {
            mocked.when(SecurityContext::getCurrentUser).thenReturn(currentUser);
            when(shopRepository.getOrganizationById(1L)).thenReturn(shopOfUser);
            assertThrows(EmptyFieldException.class,() -> shopService.editShop(1L,newShop));

        }

    }
    @Test
    void UserTryToEditShopThatIsNull_ShouldThrowException(){
        User currentUser = new User(1L, "user", true, "password", "password", "email");
        Organization shopOfUser = new Organization("shop");
        shopOfUser.setId(1L);
        shopOfUser.setOwner(currentUser);
        currentUser.getOrganizationList().add(shopOfUser);
        ShopEditDTO newShop = new ShopEditDTO();
        newShop.setName("nesShop");
        newShop.setDescription("newDescription");
        try (MockedStatic<SecurityContext> mocked = mockStatic(SecurityContext.class)) {
            mocked.when(SecurityContext::getCurrentUser).thenReturn(currentUser);
            when(shopRepository.getOrganizationById(1L)).thenReturn(null);
            assertThrows(EntityNotFoundException.class,() -> shopService.editShop(1L,newShop));

        }

    }

    @Test
    void getShopDetails_ShouldReturnShopDetails(){
        User currentUser = new User(1L, "user", true, "password", "password", "email");
        Organization expectedShop = new Organization();
        expectedShop.setId(1L);
        expectedShop.setName("shop");
        expectedShop.setDescription("description");
        expectedShop.setOwner(currentUser);
        currentUser.getOrganizationList().add(expectedShop);
        when(shopRepository.getOrganizationById(1L)).thenReturn(expectedShop);
        Organization actualShop = shopService.getShopDetails(1L);
        assertNotNull(actualShop);
        assertEquals(actualShop,expectedShop);
        verify(shopRepository).getOrganizationById(1L);
    }
    @Test
    void getShopDetailsThatIdIsZero_ShouldReturnNull(){
        User currentUser = new User(1L, "user", true, "password", "password", "email");
        Organization expectedShop = new Organization();
        expectedShop.setName("shop");
        expectedShop.setDescription("description");
        expectedShop.setOwner(currentUser);
        currentUser.getOrganizationList().add(expectedShop);
        when(shopRepository.getOrganizationById(0)).thenReturn(null);
        Organization actualShop = shopService.getShopDetails(0);
        assertNull(actualShop);
        verify(shopRepository).getOrganizationById(0);
    }










}