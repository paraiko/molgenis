package org.molgenis.data;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class DataServiceImplTest
{
	private final List<String> entityNames = asList("Entity1", "Entity2", "Entity3");
	private Repository<Entity> repo1;
	private Repository<Entity> repo2;
	private Repository<Entity> repoToRemove;
	private DataServiceImpl dataService;
	private MetaDataService metaDataService;

	@BeforeMethod
	public void beforeMethod()
	{
		Collection<? extends GrantedAuthority> authorities = singletonList(
				new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_SU));

		Authentication authentication = mock(Authentication.class);

		doReturn(authorities).when(authentication).getAuthorities();

		when(authentication.isAuthenticated()).thenReturn(true);
		UserDetails userDetails = when(mock(UserDetails.class).getUsername()).thenReturn(SecurityUtils.AUTHORITY_SU)
				.getMock();
		when(authentication.getPrincipal()).thenReturn(userDetails);

		SecurityContextHolder.getContext().setAuthentication(authentication);

		dataService = new DataServiceImpl();
		repo1 = when(mock(Repository.class).getName()).thenReturn("Entity1").getMock();

		repo2 = mock(Repository.class);
		repo2 = when(mock(Repository.class).getName()).thenReturn("Entity2").getMock();

		repoToRemove = mock(Repository.class);
		repoToRemove = when(mock(Repository.class).getName()).thenReturn("Entity3").getMock();

		metaDataService = mock(MetaDataService.class);
		when(metaDataService.getRepository("Entity1")).thenReturn(repo1);
		when(metaDataService.getRepository("Entity2")).thenReturn(repo2);
		when(metaDataService.getRepository("Entity3")).thenReturn(repoToRemove);
		EntityMetaData entityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn("Entity1").getMock();
		EntityMetaData entityMeta2 = when(mock(EntityMetaData.class).getName()).thenReturn("Entity2").getMock();
		EntityMetaData entityMeta3 = when(mock(EntityMetaData.class).getName()).thenReturn("Entity3").getMock();

		when(metaDataService.getEntityMetaDatas()).thenAnswer(new Answer<Stream<EntityMetaData>>()
		{
			@Override
			public Stream<EntityMetaData> answer(InvocationOnMock invocation) throws Throwable
			{
				return asList(entityMeta1, entityMeta2, entityMeta3).stream();
			}
		});
		dataService.setMetaDataService(metaDataService);
	}

	@Test
	public void addStream()
	{
		Stream<Entity> entities = Stream.empty();
		dataService.add("Entity1", entities);
		verify(repo1, times(1)).add(entities);
	}

	@Test
	public void updateStream()
	{
		Stream<Entity> entities = Stream.empty();
		dataService.update("Entity1", entities);
		verify(repo1, times(1)).update(entities);
	}

	@Test
	public void deleteStream()
	{
		Stream<Entity> entities = Stream.empty();
		dataService.delete("Entity1", entities);
		verify(repo1, times(1)).delete(entities);
	}

	@Test
	public void deleteAllStream()
	{
		Stream<Object> entityIds = Stream.empty();
		dataService.deleteAll("Entity1", entityIds);
		verify(repo1, times(1)).deleteAll(entityIds);
	}

	@Test
	public void getEntityNames()
	{
		assertEquals(dataService.getEntityNames().collect(toList()), asList("Entity1", "Entity2", "Entity3"));
	}

	@Test
	public void getRepositoryByEntityName()
	{
		assertEquals(dataService.getRepository("Entity1"), repo1);
		assertEquals(dataService.getRepository("Entity2"), repo2);
	}

	@Test
	public void findOneStringObjectFetch()
	{
		Object id = 0;
		Fetch fetch = new Fetch();
		Entity entity = mock(Entity.class);
		when(repo1.findOneById(id, fetch)).thenReturn(entity);
		assertEquals(dataService.findOneById("Entity1", id, fetch), entity);
		verify(repo1, times(1)).findOneById(id, fetch);
	}

	@Test
	public void findOneStringObjectFetchEntityNull()
	{
		Object id = 0;
		Fetch fetch = new Fetch();
		when(repo1.findOneById(id, fetch)).thenReturn(null);
		assertNull(dataService.findOneById("Entity1", id, fetch));
		verify(repo1, times(1)).findOneById(id, fetch);
	}

	@Test
	public void findOneStringObjectFetchClass()
	{
		Object id = 0;
		Fetch fetch = new Fetch();
		Class<Entity> clazz = Entity.class;
		Entity entity = mock(Entity.class);
		when(repo1.findOneById(id, fetch)).thenReturn(entity);
		// how to check return value? converting iterable can't be mocked.
		dataService.findOneById("Entity1", id, fetch, clazz);
		verify(repo1, times(1)).findOneById(id, fetch);
	}

	@Test
	public void findOneStringObjectFetchClassEntityNull()
	{
		Object id = 0;
		Fetch fetch = new Fetch();
		Class<Entity> clazz = Entity.class;
		when(repo1.findOneById(id, fetch)).thenReturn(null);
		assertNull(dataService.findOneById("Entity1", id, fetch, clazz));
		verify(repo1, times(1)).findOneById(id, fetch);
	}

	@Test
	public void findAllStringStream()
	{
		Object id0 = "id0";
		Stream<Object> ids = Stream.of(id0);
		Entity entity0 = mock(Entity.class);
		when(repo1.findAll(ids)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", ids);
		assertEquals(entities.collect(toList()), singletonList(entity0));
	}

	@Test
	public void findAllStringStreamClass()
	{
		Object id0 = "id0";
		Stream<Object> ids = Stream.of(id0);
		Entity entity0 = mock(Entity.class);
		Class<Entity> clazz = Entity.class;
		when(repo1.findAll(ids)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", ids, clazz);
		assertEquals(entities.collect(toList()), singletonList(entity0));
	}

	@Test
	public void findAllStringStreamFetch()
	{
		Object id0 = "id0";
		Stream<Object> ids = Stream.of(id0);
		Entity entity0 = mock(Entity.class);
		Fetch fetch = new Fetch();
		when(repo1.findAll(ids, fetch)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", ids, fetch);
		assertEquals(entities.collect(toList()), singletonList(entity0));
	}

	@Test
	public void findAllStringStreamFetchClass()
	{
		Object id0 = "id0";
		Stream<Object> ids = Stream.of(id0);
		Entity entity0 = mock(Entity.class);
		Class<Entity> clazz = Entity.class;
		Fetch fetch = new Fetch();
		when(repo1.findAll(ids, fetch)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", ids, fetch, clazz);
		assertEquals(entities.collect(toList()), singletonList(entity0));
	}

	@Test
	public void findAllStreamString()
	{
		Entity entity0 = mock(Entity.class);
		when(repo1.findAll(new QueryImpl<>())).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1");
		assertEquals(entities.collect(toList()), singletonList(entity0));
	}

	@Test
	public void findAllStreamStringClass()
	{
		Class<Entity> clazz = Entity.class;
		Entity entity0 = mock(Entity.class);
		when(repo1.findAll(new QueryImpl<>())).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", clazz);
		assertEquals(entities.collect(toList()), singletonList(entity0));
	}

	@Test
	public void findAllStreamStringQuery()
	{
		Entity entity0 = mock(Entity.class);
		Query<Entity> query = mock(Query.class);
		when(repo1.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", query);
		assertEquals(entities.collect(toList()), singletonList(entity0));
	}

	@Test
	public void findAllStreamStringQueryClass()
	{
		Class<Entity> clazz = Entity.class;
		Entity entity0 = mock(Entity.class);
		Query<Entity> query = mock(Query.class);
		when(repo1.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = dataService.findAll("Entity1", query, clazz);
		assertEquals(entities.collect(toList()), singletonList(entity0));
	}
}
