package org.molgenis.security.permission;

import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MolgenisPermissionServiceImplTest
{
	private static Authentication AUTHENTICATION;

	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void setUpBeforeClass()
	{
		AUTHENTICATION = SecurityContextHolder.getContext().getAuthentication();

		Authentication authentication = mock(Authentication.class);
		GrantedAuthority authority1 = when(mock(GrantedAuthority.class).getAuthority())
				.thenReturn(SecurityUtils.AUTHORITY_ENTITY_PREFIX + Permission.READ + "_ENTITY1").getMock();
		GrantedAuthority authority2 = when(mock(GrantedAuthority.class).getAuthority())
				.thenReturn(SecurityUtils.AUTHORITY_ENTITY_PREFIX + Permission.WRITE + "_ENTITY2").getMock();
		GrantedAuthority authority3 = when(mock(GrantedAuthority.class).getAuthority())
				.thenReturn(SecurityUtils.AUTHORITY_ENTITY_PREFIX + Permission.COUNT + "_ENTITY3").getMock();
		GrantedAuthority authority4 = when(mock(GrantedAuthority.class).getAuthority())
				.thenReturn(SecurityUtils.AUTHORITY_PLUGIN_PREFIX + Permission.READ + "_PLUGIN1").getMock();
		GrantedAuthority authority5 = when(mock(GrantedAuthority.class).getAuthority())
				.thenReturn(SecurityUtils.AUTHORITY_PLUGIN_PREFIX + Permission.WRITE + "_PLUGIN2").getMock();
		GrantedAuthority authority6 = when(mock(GrantedAuthority.class).getAuthority())
				.thenReturn(SecurityUtils.AUTHORITY_PLUGIN_PREFIX + Permission.COUNT + "_PLUGIN3").getMock();

		when((Collection<GrantedAuthority>) (authentication.getAuthorities())).thenReturn(
				Arrays.<GrantedAuthority>asList(authority1, authority2, authority3, authority4, authority5,
						authority6));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@AfterClass
	public static void tearDownAfterClass()
	{
		SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION);
	}

	@Test
	public void hasPermissionOnEntity()
	{
		assertTrue(new MolgenisPermissionServiceImpl().hasPermissionOnEntity("entity1", Permission.READ));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnEntity("entity1", Permission.WRITE));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnEntity("entity1", Permission.COUNT));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnEntity("entity2", Permission.READ));
		assertTrue(new MolgenisPermissionServiceImpl().hasPermissionOnEntity("entity2", Permission.WRITE));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnEntity("entity2", Permission.COUNT));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnEntity("entity3", Permission.READ));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnEntity("entity3", Permission.WRITE));
		assertTrue(new MolgenisPermissionServiceImpl().hasPermissionOnEntity("entity3", Permission.COUNT));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnEntity("entity-unknown", Permission.READ));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnEntity("entity-unknown", Permission.WRITE));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnEntity("entity-unknown", Permission.COUNT));
	}

	@Test
	public void hasPermissionOnPlugin()
	{
		assertTrue(new MolgenisPermissionServiceImpl().hasPermissionOnPlugin("plugin1", Permission.READ));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnPlugin("plugin1", Permission.WRITE));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnPlugin("plugin1", Permission.COUNT));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnPlugin("plugin2", Permission.READ));
		assertTrue(new MolgenisPermissionServiceImpl().hasPermissionOnPlugin("plugin2", Permission.WRITE));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnPlugin("plugin2", Permission.COUNT));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnPlugin("plugin3", Permission.READ));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnPlugin("plugin3", Permission.WRITE));
		assertTrue(new MolgenisPermissionServiceImpl().hasPermissionOnPlugin("plugin3", Permission.COUNT));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnPlugin("plugin-unknown", Permission.READ));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnPlugin("plugin-unknown", Permission.WRITE));
		assertFalse(new MolgenisPermissionServiceImpl().hasPermissionOnPlugin("plugin-unknown", Permission.COUNT));
	}
}
