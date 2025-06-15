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

    public List<User> findPaged(int page,int size){
        try {
            return userDao.findPaged(page*size,size);
        }catch(Exception e){ throw new RuntimeException(e);}
    }
} 