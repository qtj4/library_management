package edu.epam.fop.service;

import edu.epam.fop.dao.UserDao;
import edu.epam.fop.dao.RoleDao;
import edu.epam.fop.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDao userDao;
    private final RoleDao roleDao;

    public CustomUserDetailsService(UserDao userDao, RoleDao roleDao) {
        this.userDao = userDao;
        this.roleDao = roleDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            var user = userDao.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }
            // Load roles
            user.getRoles().addAll(roleDao.findByUserId(user.getId()));
            return new CustomUserDetails(user);
        } catch(Exception e){
            if(e instanceof UsernameNotFoundException) throw (UsernameNotFoundException)e;
            throw new UsernameNotFoundException("User lookup failed", e);
        }
    }
} 