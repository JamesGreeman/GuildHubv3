package com.zanvork.guildhubv3.services;

import com.zanvork.guildhubv3.model.OwnedEntity;
import com.zanvork.guildhubv3.model.OwnedEntityOwnershipRequest;
import com.zanvork.guildhubv3.model.Role;
import com.zanvork.guildhubv3.model.User;
import com.zanvork.guildhubv3.model.dao.OwnedEntityOwnershipRequestDAO;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author zanvork
 * @param <E>
 */
public abstract class OwnedEntityBackendService<E extends OwnedEntity> implements BackendService {
    
    
    @Autowired
    protected UserService userService;
    
    @Autowired
    protected OwnedEntityOwnershipRequestDAO ownershipRequestDAO;
    
    protected Map<Long, E> entities           =   new HashMap<>();
    protected Map<String, E> entitiesByName   =   new HashMap<>();
    
    protected Map<Long, OwnedEntityOwnershipRequest> ownershipRequests  =   new HashMap<>();
    
    
    protected final Object
            entitiesLock            =   new Object(),
            entitiesByNameLock      =   new Object(),
            ownershipRequestsLock   =   new Object();
    
    
    public OwnedEntityOwnershipRequest getOwnershipRequest(long id)
            throws EntityNotFoundException{
        
        OwnedEntityOwnershipRequest ownershipRequest;
        synchronized(ownershipRequestsLock){
            ownershipRequest = ownershipRequests.get(id);
        }
        if (ownershipRequest == null){
            throw new EntityNotFoundException(
                    "Could not load OwnedEntityOwnershipRequest with id '" + id + "'."
            );
        }
        return ownershipRequest;
        
    }
    
    public E getEntity(long id)
            throws EntityNotFoundException{
        
        E entity;
        synchronized(entitiesLock){
            entity = entities.get(id);
        }
        if (entity == null){
            throw new EntityNotFoundException(
                    "Could not load entity with id '" + id + "'."
            );
        }
        return entity;
    }
    
    protected E getEntity(String key)
            throws EntityNotFoundException{
        
        E entity;
        synchronized(entitiesByNameLock){
            entity = entitiesByName.get(key);
        }
        if (entity == null){
            throw new EntityNotFoundException(
                    "Could not load entity with key '" + key + "'."
            );
        }
        return entity;
    }
    
    protected boolean entityExists(long id){
        synchronized(entitiesLock){
            return entities.containsKey(id);
        }
    }
    
    protected boolean entityExists(String key){
        synchronized(entitiesByNameLock){
            return entitiesByName.containsKey(key);
        }
    }
    
    public E takeOwnership(User user, long entityId){
        E entity =   getEntity(entityId);
        canChangeEntityOwner(user, entity);
        entity.setOwner(user);
        saveEntity(entity);
        
        return entity;
    }
    
    public OwnedEntityOwnershipRequest requestOwnship(User user, long entityId)
            throws EntityNotFoundException, ReadOnlyEntityException{
        
        E entity    =   getEntity(entityId);
        
        try {
            canChangeEntityOwner(user, entity);
        } catch (NotAuthorizedException e){
            //We don't care about not authorized exceptions, but other exceptions still matter
        } 
        OwnedEntityOwnershipRequest ownershipRequest  =   new OwnedEntityOwnershipRequest();
        ownershipRequest.setDateCreated(new Date());
        ownershipRequest.setRequester(user);
        ownershipRequest.setCurrentOwner(entity.getOwner());
        ownershipRequest.setSubjectId(entityId);
        ownershipRequest.setEntityType(entity.getClass().getSimpleName());
        
        saveOwnershipRequest(ownershipRequest);
        return ownershipRequest;
    }
    
    public E approveOwnershipRequest(User user, long entityId, long ownershipRequestId){
        OwnedEntityOwnershipRequest ownershipRequest    =   getOwnershipRequest(ownershipRequestId);
        if (entityId != ownershipRequest.getSubjectId()){
            throw new UnexpectedEntityException(
                    "Entity id expected ('" + entityId + 
                    "') does not match the id in the ownership request ('" + 
                    ownershipRequest.getSubjectId() + "')."
            );
        }
        E entity    =   getEntity(entityId);
        if (ownershipRequest.getCurrentOwner().getId() != entity.getOwner().getId()){
            rejectOwnershipRequest(user, ownershipRequestId);
            throw new UnexpectedEntityException(
                    "Owner id expected ('" + ownershipRequest.getCurrentOwner().getId() + 
                    "') does not match the owner of the entity now ('" + 
                    entity.getOwner().getId() + "')."
            );
        }
        entity  =   changeUser(user, entity, ownershipRequest.getRequester().getId());
        deleteOwnershipRequest(ownershipRequest);
        return entity;
        
    }
    
    public void rejectOwnershipRequest(User user, long ownershipRequestId){
        OwnedEntityOwnershipRequest ownershipRequest    =   getOwnershipRequest(ownershipRequestId);
        
        deleteOwnershipRequest(ownershipRequest);
        
    }
    
    public E changeUser(User user, long entityId, long userId){
        E entity =   getEntity(entityId);
        return changeUser(user, entity, userId);
    }
    
    public E changeUser(User user, E entity, long userId)
            throws EntityNotFoundException, ReadOnlyEntityException, OwnershipLockedException, NotAuthorizedException{
        
        User newUser    =   userService.getUser(userId);
        canChangeEntityOwner(newUser, entity);
        entity.setOwner(newUser);
        saveEntity(entity);
        return entity;
    }
    
     public E setEntityLocked(User user, long id, boolean locked)
            throws EntityNotFoundException, ReadOnlyEntityException, NotAuthorizedException{
        
        E entity =   getEntity(id);
        userCanEditEntity(user, entity);
        entity.setOwnershipLocked(locked);
        saveEntity(entity);
        return entity;
    }
    
    
    protected boolean canChangeEntityOwner(User user, E entity)
            throws OwnershipLockedException, NotAuthorizedException{
        
         String errorText    =   "Cannot change ownership of entity with name '" + entityToKey(entity) + "'.";
        //Check the entity is not read only
        if (entity.isOwnershipLocked()){
            throw new OwnershipLockedException(
                    errorText + "  It has been had it's ownership locked."
            );
        }
        userCanEditEntity(user, entity);
        return true;
    }
    
    
    protected boolean userCanEditEntity(User user, E entity)
            throws ReadOnlyEntityException, NotAuthorizedException{
        
        String errorText    =   "Cannot update entity with key '" + entityToKey(entity) + "'.";
        
        //Take ownership of a entity when updating it if not already owned.
        if (entity.getOwner() == null && entity.isOwnershipLocked()){
            entity.setOwner(user);
        }
        //Check the entity is not read only
        if (entity.isReadOnly()){
            throw new ReadOnlyEntityException(
                    errorText + "  It has been flagged as read only"
            );
        }
        //Check if user is an admin
        if (!user.hasRole(Role.ROLE_ADMIN)){
            //if entity is owned by user 
            if (user.getId() != entity.getOwner().getId()){
                throw new NotAuthorizedException(
                        errorText + "  User does has neither admin rights nor owns the object"
                );
            }
        }
        return true;
    }
    
    @Override
    public void updateFromBackend(){
        loadEntitiesFromBackend();
        loadOwnershipRequestsFromBackend();
    }
    
    public void saveOwnershipRequest(OwnedEntityOwnershipRequest ownershipRequest){
        ownershipRequestDAO.save(ownershipRequest);
       
        synchronized(ownershipRequestsLock){
            ownershipRequests.put(ownershipRequest.getId(), ownershipRequest);
        }
    }
    
    public void deleteOwnershipRequest(OwnedEntityOwnershipRequest ownershipRequest){
        ownershipRequestDAO.delete(ownershipRequest);
       
        synchronized(ownershipRequestsLock){
            ownershipRequests.remove(ownershipRequest.getId());
        }
    }
    
    protected abstract void saveEntity(E entity);
    
    public abstract String entityToKey(E entity);
    
    protected void loadOwnershipRequestsFromBackend(){
        Map<Long, OwnedEntityOwnershipRequest> newOwnershipRequests          =   new HashMap<>();
        ownershipRequestDAO.findAll().forEach(ownershipRequest -> {
            newOwnershipRequests.put(ownershipRequest.getId(), ownershipRequest);
        });
        synchronized (ownershipRequestsLock){
            ownershipRequests    =   newOwnershipRequests;
        }
    }
    
    /**
     * Load all entities from the entityDAO and store them in the entities 
     * map in the service.
     * Uses Entity's id as key
     */    
    protected abstract void loadEntitiesFromBackend();
  /*      
    }*/
}
