package sk.ppmscan.application.importexport.hibernate;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.ppmscan.application.beans.ScanRun;
import sk.ppmscan.application.hibernate.HibernateUtil;
import sk.ppmscan.application.importexport.FilteredTeamsExporter;

public class FilteredManagersHibernateExporter implements FilteredTeamsExporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(FilteredManagersHibernateExporter.class);
	
	@Override
	public void exportData(ScanRun scanRun) throws Exception {
		Session session = HibernateUtil.getSessionFactory().openSession();
		
		long startTime = System.currentTimeMillis();

		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.saveOrUpdate(scanRun);
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

	@Override
	public ScanRun importData() throws Exception {
		throw new Exception("Not supported");
	}

}
