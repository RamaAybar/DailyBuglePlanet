/*
// Curso Egg FullStack
 */
package com.dailybugleplanet.DailyBuglePlanet.services;

// @author Ramiro Aybar
import com.dailybugleplanet.DailyBuglePlanet.entities.Account;
import com.dailybugleplanet.DailyBuglePlanet.entities.Image;
import com.dailybugleplanet.DailyBuglePlanet.enums.Roles;
import com.dailybugleplanet.DailyBuglePlanet.exceptions.NewsException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpSession;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import com.dailybugleplanet.DailyBuglePlanet.repositories.AccountRepository;

@Service
public class AccountService implements UserDetailsService {

    @Autowired
    private AccountRepository repository;

    @Autowired
    private ImageService imageService;

    private final BCryptPasswordEncoder pswdEncoder;

    public AccountService() {
        this.pswdEncoder = new BCryptPasswordEncoder();
    }

    //Métodos relacionados al "signIn" y "signUp"
    @Transactional
    public void signup(String name, String password,
            String confirm, Roles role,
            MultipartFile photo) throws NewsException {
        validate(name, password, photo);

        if (null == confirm || !password.equals(confirm)) {
            throw new NewsException("Ambas contraseñas deben coincidir.");
        }
        Account user = new Account();
        setData(user, name, password, role, photo);
        repository.save(user);
    }

    private void setData(Account user, String name, String password, Roles role, MultipartFile photo) throws NewsException {
        user.setName(name);
        user.setPassword(pswdEncoder.encode(password));
        user.setAccountType(role);
        Image image = imageService.save(photo);
        user.setImage(image);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (null == username || username.isEmpty()) {
            throw new UsernameNotFoundException("El nombre no es válido");
        }

        Account newsUser = repository.searchUserByName(username);
        if (null == newsUser) {
            throw new UsernameNotFoundException("No se ha encontrado el usuario.");
        }

        List<GrantedAuthority> permissions = new ArrayList();
        GrantedAuthority grantedAuth = new SimpleGrantedAuthority("ROLE_" + newsUser.getAccountType().toString());
        permissions.add(grantedAuth);

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession(true);
        session.setAttribute("userSession", newsUser);
        return new User(newsUser.getName(), newsUser.getPassword(), permissions);
    }

    //Métodos relacionados a cambios en las cuentas
    @Transactional
    public void update(String id, String name,
            String password, String confirm,
            MultipartFile newImage) throws NewsException {
        validate(id, name, password);

        if (!pswdEncoder.matches(confirm, password)) {
            throw new NewsException("Contraseña incorrecta, vuelva a intentarlo.");
        }

        Account user = getUserById(id);
        user.setName(name);

        //Si el usuario no posteó una foto, se mantiene la foto anterior.
        if (null != newImage && !newImage.isEmpty()) {
            user.setImage(updateImage(user.getImage(), newImage));
        }

        repository.save(user);
    }

    //Si la cuenta no tenía una imagen asociada, genero una, de lo contrario modifico la actual.
    private Image updateImage(Image oldImage, MultipartFile newImage) throws NewsException {
        return null == oldImage || null == oldImage.getId() ? imageService.save(newImage) : imageService.update(newImage, oldImage.getId());

    }

    //Relacionado a "GET" usuarios.
    @Transactional(readOnly = true)
    public Account getUserById(String id) throws NewsException {
        return repository.searchAccountById(id);
    }

    @Transactional(readOnly = true)
    public Account getJournalistById(String id) throws NewsException {
        return repository.searchJournalistById(id);
    }

    @Transactional(readOnly = true)
    public Account getAdminById(String id) throws NewsException {
        return repository.searchAdminById(id);
    }

    @Transactional(readOnly = true)
    public List<Account> getJournalistsAndAdmins() {
        List<Account> journalistAndAdmins = new ArrayList();
        journalistAndAdmins.addAll(repository.getAllAdmins());
        journalistAndAdmins.addAll(repository.getAllJournalists());
        return journalistAndAdmins;
    }

    @Transactional
    //SOFT DELETE (Cambiar el tipo de cuenta a "User")
    public void dismissJournalist(String id) throws NewsException {
        Account user = getUserById(id);
        if (null == user) {
            throw new NewsException("No se ha encontrado la cuenta.");
        }
        if (user.getAccountType() != Roles.JOURNALIST) {
            throw new NewsException("La cuenta pertenece a " + user.getAccountType() + ", no se puede descartar.");
        }
        user.setAccountType(Roles.USER);
        repository.save(user);
    }

    //Validaciones
    private void validate(String name, String password) throws NewsException {
        if (null == name || name.isEmpty()) {
            throw new NewsException("El nombre ingresado no es válido.");
        }

        if (null == password || password.isEmpty() || 8 > password.length()) {
            throw new NewsException("La contraseña ingresada no es válida, inténtelo de nuevo.");
        }
    }

    private void validate(String name, String password, MultipartFile photo) throws NewsException {
        validate(name, password);
        if (null == photo || photo.isEmpty()) {
            throw new NewsException("La imágen ingresada no es válida.");
        }
    }

    private void validate(String id, String name, String password) throws NewsException {
        validate(name, password);
        if (null == id || id.isEmpty()) {
            throw new NewsException("El ID ingresado no es válido.");
        }
    }
}
