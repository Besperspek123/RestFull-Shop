package spring.rest.shop.springrestshop.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import spring.rest.shop.springrestshop.aspect.SecurityContext;
import spring.rest.shop.springrestshop.entity.Organization;
import spring.rest.shop.springrestshop.entity.Role;
import spring.rest.shop.springrestshop.entity.User;
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


}