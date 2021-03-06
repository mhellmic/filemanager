/* 
 * Created on Dec 11, 2007
 * 
 * Developed by: Rion Dooley - dooley [at] tacc [dot] utexas [dot] edu
 * 				 TACC, Texas Advanced Computing Center
 * 
 * https://www.tacc.utexas.edu/
 */

package org.teragrid.portal.filebrowser.server.servlet.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
//import org.teragrid.portal.filebrowser.applet.util.logger.
import org.teragrid.portal.filebrowser.server.servlet.exception.PersistenceException;
import org.teragrid.portal.filebrowser.server.servlet.model.notification.Notification;
import org.teragrid.portal.filebrowser.server.servlet.persistence.HibernateUtil;

/**
 * Transaction class for Notification reference objects wrapping
 * db queries and actions.
 * 
 * @author Rion Dooley < dooley [at] tacc [dot] utexas [dot] edu >
 *
 */
@SuppressWarnings("unchecked")
public class NotificationDAO {
	private static final Logger logger = Logger.getLogger(NotificationDAO.class);
	
    public NotificationDAO() {
        HibernateUtil.beginTransaction();
    }

    // ********************************************************** //

    public static Notification getNotificationById(Integer nId, boolean lock)
            throws PersistenceException {
        HibernateUtil.beginTransaction();
        
        Session session = HibernateUtil.getSession();
        Notification notif = null;
        try {
            if (lock) {
                notif = (Notification) session.load(Notification.class, nId, LockMode.UPGRADE);
            } else {
                notif = (Notification) session.load(Notification.class, nId);
            }
        }  catch (HibernateException ex) {
            throw new PersistenceException(ex);
        } finally {
        	HibernateUtil.closeSession();
        }
        return notif;
    }

    // ********************************************************** //

    public static boolean exists(Integer notificationId) {
        HibernateUtil.beginTransaction();
        
        Session session = HibernateUtil.getSession();
        
        try {
            if (session.get(Notification.class, notificationId) == null) {
                return false;
            } else {
                return true;
            }
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        } finally {
        	HibernateUtil.closeSession();
        }
    }
    // ********************************************************** //

    public static List<Notification> getAllCompleted()
    throws PersistenceException {

        HibernateUtil.beginTransaction();
        
        Session session = HibernateUtil.getSession();
        
        List<Notification> sites = null;
        
        try {
            String sql = "from Notification n left join Transfer t on n.transferId = t.id where t.status = :done or t.status = :failed";
            sites = session.createQuery(sql)
                .setInteger("done",org.teragrid.portal.filebrowser.applet.transfer.Task.DONE)
                .setInteger("failed",org.teragrid.portal.filebrowser.applet.transfer.Task.FAILED)
                        .list();
            
        }  catch (HibernateException ex) {
            throw new PersistenceException(ex);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        } finally {
        	HibernateUtil.closeSession();
        }
        
        return sites;
    }
    
    public static List<Notification> getAllByTransferID(Integer transferID)
            throws PersistenceException {

        HibernateUtil.beginTransaction();
        
        Session session = HibernateUtil.getSession();
        
        List<Notification> sites = null;
        
        try {
            
            sites = session.createCriteria(Notification.class)
                .add(Expression.eq("transferId",transferID)).list();
            
        }  catch (HibernateException ex) {
            throw new PersistenceException(ex);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        } finally {
        	HibernateUtil.closeSession();
        }
        
        return sites;
    }
    
    public static List<Notification> get(Integer transferId, String wholeName)
    throws PersistenceException {

        HibernateUtil.beginTransaction();
        
        Session session = HibernateUtil.getSession();
        
        List<Notification> sites = null;
        
        try {
            
            sites = session.createCriteria(Notification.class)
                .add(Expression.eq("transferId",transferId))
                .add(Expression.eq("username",wholeName)).list();
            
        }  catch (HibernateException ex) {
            throw new PersistenceException(ex);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        } finally {
        	HibernateUtil.closeSession();
        }
        
        return sites;
    }
    
    public static List<Notification> get(String wholeName)
    throws PersistenceException {

        HibernateUtil.beginTransaction();
        
        Session session = HibernateUtil.getSession();
        
        List<Notification> sites = null;
        
        try {
            
            sites = session.createCriteria(Notification.class)
                .add(Expression.eq("username",wholeName)).list();
            
        }  catch (HibernateException ex) {
            throw new PersistenceException(ex);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        } finally {
        	HibernateUtil.closeSession();
        }
        
        return sites;
    }
    // ********************************************************** //
    
    public static List<Notification> getByExample(Notification notif)
    throws PersistenceException {
        
        HibernateUtil.beginTransaction();
        
        Session session = HibernateUtil.getSession();
        
        List<Notification> sites = null;
        
        try {
            
            sites = session.createCriteria(Notification.class)
                .add(Expression.eq("transferId",notif.getTransferId()))
                .add(Expression.eq("type", notif.getType()))
                .add(Expression.eq("username",notif.getUsername())).list();
            
        }  catch (HibernateException ex) {
            throw new PersistenceException(ex);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        } finally {
        	HibernateUtil.closeSession();
        }
        
        return sites;
    }
    
    public static boolean exists(List<Notification> notifications) {
        HibernateUtil.beginTransaction();
        
        Session session = HibernateUtil.getSession();
        
        List<Notification> sites = null;
        
        try {
            
            String sql = "from Notification n where n.type = :type and n.username = :username and";
            
            for (Notification notification: notifications) {
                sql += " or transferId = :transferId" + notification.getTransferId().intValue();
            }
           
	    sql = sql.replaceFirst("and or","and");

            Query query = session.createQuery(sql)
                .setString("type", notifications.get(0).getType().name())
                .setString("username",notifications.get(0).getUsername());
 
            for (Notification notification: notifications) {
                query.setInteger("transferId"+notification.getTransferId().intValue(), notification.getTransferId());
            }
            
            sites = query.list();
            
            if (sites == null || sites.size() > 1) {
                return false;
            } else if (sites.size() != notifications.size()){
                return false;
            } else {
                return true;
            }
            
        }  catch (HibernateException ex) {
            logger.error("Failed to locate verify all transfer ids",ex);
            throw new PersistenceException(ex);
        } finally {
        	HibernateUtil.closeSession();
        }
    }

    // ********************************************************** //
    
    public static void makePersistent(Object notifications)
    throws PersistenceException {

        HibernateUtil.beginTransaction();
        
        try {
            
            Session s = HibernateUtil.getSession();
            
            Transaction tx = s.getTransaction();
            
            tx.begin();
            
            if (notifications instanceof ArrayList) {
                for(Notification notification: (ArrayList<Notification>)notifications) {
                    s.saveOrUpdate(notification);
                }
            } else {
                s.saveOrUpdate(notifications);
            }
            
            tx.commit();
            
            s.flush();
            
            s.clear();
            
            s.evict(notifications);
            
            s.close();
            
        } catch (Exception e) {
            logger.error("Failed to finish add",e);
            HibernateUtil.rollbackTransaction();
            throw new PersistenceException(e);
        } finally {
            HibernateUtil.closeSession();
        }
    }
    
    // ********************************************************** //
    
    public static void makeTransient(Object notifications)
        throws PersistenceException {
    
        HibernateUtil.beginTransaction();
        try {
            
            Session s = HibernateUtil.getSession();
            
            Transaction tx = s.getTransaction();
            
            tx.begin();
            
            if (notifications instanceof ArrayList) {
                for(Notification notification: (ArrayList<Notification>)notifications) {
                    s.delete(notification);
                }
            } else {
                s.delete(notifications);
            }
            
            tx.commit();
            
            s.flush();
            
            s.clear();
            
            s.evict(notifications);
            
            s.close();
            
        } catch (Exception e) {
            logger.error("Failed to finish delete",e);
            HibernateUtil.rollbackTransaction();
            throw new PersistenceException(e);
        } finally {
            HibernateUtil.closeSession();
        }
    }
}
