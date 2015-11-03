package com.github.ndionisi.hibernatelevel2;

import com.github.ndionisi.hibernatelevel2.HibernateLevel2CacheApplication;
import com.github.ndionisi.hibernatelevel2.domain.AddressReadWrite;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = HibernateLevel2CacheApplication.class)
public class HibernateLevel2CacheReadWriteTest {

    @Inject
    private EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;

    private Long ID;

    @Before
    public void setUp() throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        AddressReadWrite address = new AddressReadWrite();
        address.setCity("Paris");
        entityManager.persist(address);

        transaction.commit();

        ID = address.getId();

        this.entityManager = entityManagerFactory.createEntityManager();
    }

    @After
    public void tearDown() throws Exception {
        EntityTransaction transaction = entityManager.getTransaction();
        if(transaction.isActive()) {
            transaction.commit();
        }
    }

    @Test
    public void checkEntityIsNotIn2ndLevelCacheAfterPersist() throws Exception {
        assertThat(entityManagerFactory.getCache().contains(AddressReadWrite.class, ID), is(false));
    }

    @Test
    public void checkEntityIsIn2ndLevelCacheAfterSessionCleared() throws Exception {

        entityManager.getTransaction().begin();

        AddressReadWrite address = entityManager.find(AddressReadWrite.class, ID);

        assertThat(address, notNullValue());

        assertThat(entityManager.contains(address), is(true));

        entityManager.clear();

        assertThat(entityManager.contains(address), is(false));

        assertThat(entityManagerFactory.getCache().contains(AddressReadWrite.class, address.getId()), is(true));
    }

    @Test
    public void checkCachedEntitiesAreNotUpdatedInCacheBeforeFlush() {
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();

        AddressReadWrite address = entityManager.find(AddressReadWrite.class, ID);

        assertThat(entityManagerFactory.getCache().contains(AddressReadWrite.class, ID), is(true));

        address.setCity("Los Angeles");

        assertThat(entityManagerFactory.getCache().contains(AddressReadWrite.class, ID), is(true));

        // read from level 2 cache
        AddressReadWrite address2 = entityManager2.find(AddressReadWrite.class, ID);
        assertThat(address2.getCity(), is("Paris"));
    }

    @Test
    public void checkEntityNotEvictedFromCacheOnFlush() throws Exception {
        entityManager.getTransaction().begin();

        AddressReadWrite address = entityManager.find(AddressReadWrite.class, ID);
        address.setCity("Los Angeles");

        assertThat(entityManagerFactory.getCache().contains(AddressReadWrite.class, ID), is(true));

        entityManager.flush();

        assertThat(entityManagerFactory.getCache().contains(AddressReadWrite.class, ID), is(true));
    }

    @Test
    public void checkEntityNotEvictedFromCacheOnCommit() throws Exception {
        entityManager.getTransaction().begin();

        AddressReadWrite address = entityManager.find(AddressReadWrite.class, ID);
        address.setCity("Los Angeles");

        assertThat(entityManagerFactory.getCache().contains(AddressReadWrite.class, ID), is(true));

        entityManager.flush();

        entityManager.getTransaction().commit();

        assertThat(entityManagerFactory.getCache().contains(AddressReadWrite.class, ID), is(true));
    }

    @Test
    public void checkOtherEntityManagerDoesNotSeeUncommitedData() throws Exception {
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();

        AddressReadWrite address = entityManager.find(AddressReadWrite.class, ID);
        address.setCity("Los Angeles");

        entityManager.flush();

        AddressReadWrite address2 = entityManager2.find(AddressReadWrite.class, ID);
        assertThat(address2.getCity(), equalTo("Paris"));
    }

    @Test
    public void checkOtherEntityManagerSeeCommitedData() throws Exception {
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();

        AddressReadWrite address = entityManager.find(AddressReadWrite.class, ID);
        address.setCity("Los Angeles");

        entityManager.flush();
        entityManager.getTransaction().commit();

        AddressReadWrite address2 = entityManager2.find(AddressReadWrite.class, ID);
        assertThat(address2.getCity(), equalTo("Los Angeles"));
    }

    @Test
    public void checkOtherEntityManagerGoesInDbAndCurrentEntityManagerGoesInCache() throws Exception {
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();

        AddressReadWrite address = entityManager.find(AddressReadWrite.class, ID);
        address.setCity("Los Angeles");

        entityManager.flush();
        entityManager.detach(address);

        AddressReadWrite address2 = entityManager.find(AddressReadWrite.class, ID);
        assertThat(address2.getCity(), equalTo("Los Angeles"));

        AddressReadWrite address3 = entityManager2.find(AddressReadWrite.class, ID);
        assertThat(address3.getCity(), equalTo("Paris"));
    }
}
