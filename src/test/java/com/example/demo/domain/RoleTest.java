package com.example.demo.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void testRoleEnumValues() {
        // Test that all expected roles exist
        Role[] roles = Role.values();
        assertEquals(3, roles.length);
        
        assertTrue(contains(roles, Role.USER));
        assertTrue(contains(roles, Role.ADMIN));
        assertTrue(contains(roles, Role.SUPER_ADMIN));
    }

    @Test
    void testRoleNames() {
        assertEquals("USER", Role.USER.getName());
        assertEquals("ADMIN", Role.ADMIN.getName());
        assertEquals("SUPER_ADMIN", Role.SUPER_ADMIN.getName());
    }

    @Test
    void testRoleToString() {
        assertEquals("USER", Role.USER.toString());
        assertEquals("ADMIN", Role.ADMIN.toString());
        assertEquals("SUPER_ADMIN", Role.SUPER_ADMIN.toString());
    }

    @Test
    void testRoleValueOf() {
        assertEquals(Role.USER, Role.valueOf("USER"));
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
        assertEquals(Role.SUPER_ADMIN, Role.valueOf("SUPER_ADMIN"));
    }

    @Test
    void testRoleValueOf_InvalidRole_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Role.valueOf("INVALID_ROLE");
        });
    }

    @Test
    void testRoleEquality() {
        assertEquals(Role.USER, Role.USER);
        assertEquals(Role.ADMIN, Role.ADMIN);
        assertEquals(Role.SUPER_ADMIN, Role.SUPER_ADMIN);
        
        assertNotEquals(Role.USER, Role.ADMIN);
        assertNotEquals(Role.ADMIN, Role.SUPER_ADMIN);
        assertNotEquals(Role.USER, Role.SUPER_ADMIN);
    }

    @Test
    void testRoleOrdinal() {
        // Test the natural ordering of roles
        assertEquals(0, Role.USER.ordinal());
        assertEquals(1, Role.ADMIN.ordinal());
        assertEquals(2, Role.SUPER_ADMIN.ordinal());
    }

    @Test
    void testRoleComparison() {
        // Test role comparison based on ordinal
        assertTrue(Role.USER.compareTo(Role.ADMIN) < 0);
        assertTrue(Role.ADMIN.compareTo(Role.SUPER_ADMIN) < 0);
        assertTrue(Role.SUPER_ADMIN.compareTo(Role.USER) > 0);
        
        assertEquals(0, Role.USER.compareTo(Role.USER));
        assertEquals(0, Role.ADMIN.compareTo(Role.ADMIN));
        assertEquals(0, Role.SUPER_ADMIN.compareTo(Role.SUPER_ADMIN));
    }

    @Test
    void testRoleNameImmutability() {
        // Test that role names cannot be modified
        String userName = Role.USER.getName();
        assertEquals("USER", userName);
        
        // Verify that modifying the returned string doesn't affect the enum
        userName = userName.toLowerCase();
        assertEquals("USER", Role.USER.getName()); // Should still be uppercase
    }

    @Test
    void testRoleConstantReferences() {
        // Test that role constants are singletons
        assertSame(Role.USER, Role.valueOf("USER"));
        assertSame(Role.ADMIN, Role.valueOf("ADMIN"));
        assertSame(Role.SUPER_ADMIN, Role.valueOf("SUPER_ADMIN"));
    }

    private boolean contains(Role[] roles, Role target) {
        for (Role role : roles) {
            if (role == target) {
                return true;
            }
        }
        return false;
    }
}