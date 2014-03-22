package com.stockticker.logic;

import com.stockticker.User;
import com.stockticker.UserInfo;
import com.stockticker.persistence.PersistenceService;
import com.stockticker.persistence.StockTickerPersistence;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import org.jasypt.util.password.BasicPasswordEncryptor;
import org.junit.After;
import org.junit.Before;

public class UserAuthorizationTest {

    private final PersistenceService persistentence = StockTickerPersistence.INSTANCE;
    private final AuthorizationService userAuth = UserAuthorization.INSTANCE;
    private BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();

    private final String wrongPassword = "none";
    private final String newPassword = "newPass";
    
    private final User testUser = new User("test", "testpass");
    private final UserInfo testUserInfo = new UserInfo("Test", "User");
    private final User otherUser = new User("other", "otherpass");
    private final User anotherUser = new User("anotherUser", "anotherPassword");
    
    @Before
    public void setUp() {
        persistentence.createUser(testUser.getUserName(), 
                passwordEncryptor.encryptPassword(testUser.getPassword()));
        persistentence.createUser(otherUser.getUserName(), 
                passwordEncryptor.encryptPassword(otherUser.getPassword()));
    }
    
    @After
    public void tearDown() {
        persistentence.deleteUser(testUser.getUserName());
        persistentence.deleteUser(otherUser.getUserName());
        persistentence.deleteUser(anotherUser.getUserName());
    }
    
    @Test
    public void testLogIn() throws Exception {
        boolean result = userAuth.logIn(testUser.getUserName(), 
                testUser.getPassword());
        assertTrue("Successful Login Test", result);
    }

    @Test
    public void testFailedLogIn() throws Exception {
        boolean result = userAuth.logIn(testUser.getUserName(), "");
        assertFalse("Failed Login Test", result);
    }
    
    @Test
    public void testLoginWithMultipleUsersLoggedIn() throws Exception {
        persistentence.setLoginStatus(otherUser.getUserName(), true);
        boolean result = userAuth.logIn(testUser.getUserName(), testUser.getPassword());
        assertTrue("Successful Login Test with multiple users logged in", result);
    }
    
    @Test
    public void testLoginWithMultipleUsersLoggedInAndUserLoggedIn() throws Exception {
        persistentence.setLoginStatus(otherUser.getUserName(), true);
        persistentence.setLoginStatus(testUser.getUserName(), true);
        boolean result = userAuth.logIn(testUser.getUserName(), testUser.getPassword());
        assertTrue("Successful Login Test with multiple users logged in", result);
    }
    
    @Test
    public void testNonRegisteredLogIn() {
        boolean result = userAuth.logIn(anotherUser.getUserName(), anotherUser.getPassword());
        assertFalse("Log in with non registered user test", result);
    }

    @Test
    public void testLogOut() throws Exception {
        persistentence.setLoginStatus(testUser.getUserName(), true);
        boolean result = userAuth.logOut(testUser.getUserName());
        assertTrue("Log out test", result);
    }
    
    @Test
    public void testLogOutWhileNotLoggedIn() throws Exception {
        persistentence.setLoginStatus(otherUser.getUserName(), true);
        boolean result = userAuth.logOut(testUser.getUserName());
        assertFalse("Log out test when not logged in", result);
    }
    
    @Test
    public void testFailedLogOut() throws Exception {
        boolean result = userAuth.logOut(anotherUser.getUserName());
        assertFalse("Failed log out test", result);
    }

    @Test
    public void testIsLoggedIn() throws Exception {
        persistentence.setLoginStatus(testUser.getUserName(), true);
        boolean result = userAuth.isLoggedIn(testUser.getUserName());
        assertTrue("Is logged in test", result);
    }

    @Test
    public void testIsNotLoggedIn() throws Exception {
        persistentence.setLoginStatus(testUser.getUserName(), false);
        boolean result = userAuth.isLoggedIn(testUser.getUserName());
        assertFalse("Is not logged in test", result);
    }

    @Test
    public void testRegister() throws Exception {
        UserInfo anotherUserInfo = new UserInfo("Another", "User");
        boolean result = userAuth.register(anotherUser.getUserName(), 
                                            anotherUser.getPassword(), 
                                            anotherUserInfo);
        assertTrue("Register test", result);
    }
    
    @Test
    public void testFailedRegister() throws Exception {
        boolean result = userAuth.register(testUser.getUserName(), 
                                            testUser.getPassword(),
                                            testUserInfo);
        assertFalse("Failed user registeration test", result);
    }

    @Test
    public void testUnRegister() throws Exception {
        boolean result = userAuth.unRegister(testUser.getUserName());
        assertTrue("Unregister test", result);
    }
    
    @Test
    public void testFailedUnRegister() throws Exception {
        boolean result = userAuth.unRegister(anotherUser.getUserName());
        assertFalse("Unregister failed test", result);
    }

    @Test
    public void testIsRegistered() throws Exception {
        boolean result = userAuth.isRegistered(testUser.getUserName());
        assertTrue("User is registered test", result);
    }
    
    @Test
    public void testIsNotRegistered() throws Exception {
        boolean result = userAuth.isRegistered(anotherUser.getUserName());
        assertFalse("User is not registered test", result);
    }

    @Test
    public void testGetUserInfo() throws Exception {
        User user = persistentence.getUser(testUser.getUserName());
        user.setUserInfo(testUserInfo);
        persistentence.updateUser(user);
        UserInfo userInfo = userAuth.getUserInfo(testUser.getUserName());
        boolean result = (testUserInfo.getFirstName().equals(userInfo.getFirstName()))
                && (testUserInfo.getLastName().equals(userInfo.getLastName()));
        assertTrue("Get user info", result);
    }
    
    @Test
    public void testUpdateUserInfo() {
        persistentence.setLoginStatus(testUser.getUserName(), true);
        boolean result = userAuth.updateUserInfo(testUser.getUserName(), testUserInfo);
        assertTrue("Update user info test with user logged in", result);
    }
    
    @Test
    public void testFailedUpdateUserInfo() {
        boolean result = userAuth.updateUserInfo(testUser.getUserName(), testUserInfo);
        assertFalse("Update user info test with user logged out", result);
    }
    
    @Test
    public void testFailedUpdateUserInfoWithOtherUsersLoggedIn() {
        persistentence.setLoginStatus(otherUser.getUserName(), true);
        boolean result = userAuth.updateUserInfo(testUser.getUserName(), testUserInfo);
        assertFalse("Update user info test with other users logged in", result);
    }

    @Test
    public void testChangePassword() throws Exception {
        persistentence.setLoginStatus(testUser.getUserName(), true);
        boolean result = userAuth.changePassword(testUser.getUserName(), testUser.getPassword(), newPassword);
        assertTrue("Successful change password", result);
    }
    
    @Test
    public void testFailedChangePassword() throws Exception {
        persistentence.setLoginStatus(testUser.getUserName(), true);
        boolean result = userAuth.changePassword(testUser.getUserName(), wrongPassword, newPassword);
        assertFalse("Failed change password (bad password)", result);
    }
    
    @Test
    public void testFailedLoggedoutChangePassword() throws Exception {
        persistentence.setLoginStatus(testUser.getUserName(), false);
        boolean result = userAuth.changePassword(testUser.getUserName(), testUser.getPassword(), newPassword);
        assertFalse("Failed change password (logged out user)", result);
    }
}
