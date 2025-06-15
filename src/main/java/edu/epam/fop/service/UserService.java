package edu.epam.fop.service;

import edu.epam.fop.model.User;
import edu.epam.fop.model.Role;
import edu.epam.fop.dao.RoleDao;
import edu.epam.fop.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserDao userDao;
    private final RoleDao roleDao;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserDao userDao, RoleDao roleDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    /**
     * Registers a brand-new user with default role USER.
     *
     * @param username desired unique username
     * @param rawPassword plain-text password supplied by the client; will be hashed before persisting
     * @return fully populated {@link User} entity including generated id and its single USER role
     * @throws IllegalArgumentException if username already exists
     */
    public User registerUser(String username, String rawPassword) {
        try {
            if (userDao.findByUsername(username)!=null) {
                throw new IllegalArgumentException("Username already exists");
            }
            String hashed = passwordEncoder.encode(rawPassword);
            User user = edu.epam.fop.model.User.builder()
                    .username(username)
                    .password(hashed).build();
            Long userId = userDao.save(user);
            user.setId(userId);

            Role role = roleDao.findByName("USER");
            if(role==null){
                Role newRole = new Role("USER");
                Long rid = roleDao.save(newRole);
                newRole.setId(rid);
                role = newRole;
            }
            linkRole(userId, role.getId());
            user.getRoles().add(role);
            return user;
        } catch(Exception e){ throw new RuntimeException(e);}
    }

    public List<User> findAll() {
        try {
            return userDao.findAll();
        }catch(Exception e){ throw new RuntimeException(e);}
    }

    @Transactional
    /**
     * Creates a new user with explicitly supplied role names (ADMIN/LIBRARIAN/etc.).
     *
     * @param username   unique login
     * @param rawPassword plain-text password to hash
     * @param roleNames  list of existing role names to link
     * @return persisted {@link User} with roles eagerly attached
     */
    public User create(String username, String rawPassword, List<String> roleNames) {
        try {
            if (userDao.findByUsername(username)!=null) {
                throw new IllegalArgumentException("Username exists");
            }
            String encoded = passwordEncoder.encode(rawPassword);
            User u = edu.epam.fop.model.User.builder()
                    .username(username)
                    .password(encoded).build();
            Long uid = userDao.save(u);
            u.setId(uid);
            for (String rn : roleNames) {
                Role role = roleDao.findByName(rn);
                if(role!=null){
                    linkRole(uid, role.getId());
                    u.getRoles().add(role);
                }
            }
            return u;
        }catch(Exception e){ throw new RuntimeException(e);}
    }

    @Transactional
    /**
     * Toggles logical block flag for the given user.
     * @param id user id
     */
    public void toggleBlock(Long id){
        try {
            User u = userDao.findById(id);
            if(u!=null){
                u.setBlocked(!u.isBlocked());
                userDao.update(u);
            }
        }catch(Exception e){ throw new RuntimeException(e);}
    }

    @Transactional
    /**
     * Permanently removes user from storage.
     */
    public void delete(Long id){
        try {
            userDao.deleteById(id);
        }catch(Exception e){ throw new RuntimeException(e);}
    }

    // helper to insert into user_roles
    private void linkRole(Long userId, Long roleId) throws Exception {
        String sql = "INSERT INTO user_roles(user_id, role_id) VALUES (?,?) ON CONFLICT DO NOTHING";
        try (var conn = edu.epam.fop.dao.ConnectionPool.getConnection();
             var ps = conn.prepareStatement(sql)){
            ps.setLong(1, userId);
            ps.setLong(2, roleId);
            ps.executeUpdate();
        }
    }

    /**
     * Returns a page of users.
     * @param page zero-based page index
     * @param size page size
     */
    public List<User> findPaged(int page,int size){
        try {
            return userDao.findPaged(page*size,size);
        }catch(Exception e){ throw new RuntimeException(e);}
    }
} 