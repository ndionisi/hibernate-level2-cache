package com.github.ndionisi;

import com.github.ndionisi.domain.AddressReadOnly;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.RollbackException;

import static org.hamcrest.Matchers.instanceOf;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = HibernateLevel2CacheApplication.class)
public class HibernateLevel2CacheReadOnlyTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Inject
    private EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;

    private Long ID;

    @Before
    public void setUp() throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        AddressReadOnly address = new AddressReadOnly();
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
    public void checkCannotWriteReadOnlyEntity() throws Exception {
        entityManager.getTransaction().begin();
        AddressReadOnly address2 = entityManager.find(AddressReadOnly.class, ID);
        address2.setCity("Los Angeles");

        expectedException.expect(RollbackException.class);
        expectedException.expectCause(instanceOf(UnsupportedOperationException.class));

        entityManager.getTransaction().commit();
    }
}
