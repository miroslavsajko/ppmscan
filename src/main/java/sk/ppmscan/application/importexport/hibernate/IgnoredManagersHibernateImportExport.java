package sk.ppmscan.application.importexport.hibernate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.ppmscan.application.beans.IgnoredManager;
import sk.ppmscan.application.hibernate.HibernateUtil;
import sk.ppmscan.application.importexport.IgnoredManagersImportExport;

public class IgnoredManagersHibernateImportExport implements IgnoredManagersImportExport {

	private static final Logger LOGGER = LoggerFactory.getLogger(IgnoredManagersHibernateImportExport.class);

	@Override
	public Set<Long> importData() throws Exception {
		long startTime = System.currentTimeMillis();
		
		Session session = HibernateUtil.getSessionFactory().openSession();

		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = cb.createQuery(Long.class);
		Root<IgnoredManager> root = criteriaQuery.from(IgnoredManager.class);
		
		criteriaQuery.select(root.get("managerId"));
		List<Long> result = session.createQuery(criteriaQuery).getResultList();
		
		LOGGER.info("Successfully selected {} rows", result.size());
		LOGGER.info("The operation took {} ms", System.currentTimeMillis() - startTime);
		
		session.close();
		
		return result.stream().collect(Collectors.toSet());
	}

	@Override
	public void exportData(Set<Long> data) throws Exception {
		long startTime = System.currentTimeMillis();
		
		Session session = HibernateUtil.getSessionFactory().openSession();

		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			for (Long managerId : data) {
				session.saveOrUpdate(new IgnoredManager(managerId));
			}
			
			tx.commit();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			LOGGER.error("Error during database communication!");
			throw e;
		} finally {
			session.close();
		}
		
		LOGGER.info("The operation took {} ms", System.currentTimeMillis() - startTime);

	}

}
