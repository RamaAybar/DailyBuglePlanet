/*
// Curso Egg FullStack
 */
package com.dailybugleplanet.DailyBuglePlanet.services;

// @author Ramiro Aybar

import com.dailybugleplanet.DailyBuglePlanet.entities.Account;
import com.dailybugleplanet.DailyBuglePlanet.entities.News;
import com.dailybugleplanet.DailyBuglePlanet.enums.Roles;
import com.dailybugleplanet.DailyBuglePlanet.exceptions.NewsException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.dailybugleplanet.DailyBuglePlanet.repositories.NewsRepository;

@Service
public class NewsService {

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ImageService imageService;

    @Transactional
    public void createNews(String title, String body,
            MultipartFile photo, String journalistId) throws NewsException {

        validateData(title, body, photo);
        if (null == journalistId || journalistId.isEmpty()) {
            throw new NewsException("El ID del periodista no es válido.");
        }
        News newNews = new News();
        newNews.setTitle(title);
        newNews.setBody(body);
        newNews.setReleaseDate(new Date(System.currentTimeMillis()));
        newNews.setImage(imageService.save(photo));
        setCreator(newNews, journalistId);
        newsRepository.save(newNews);
    }

    private void setCreator(News news, String journalistId) throws NewsException {
        Account journalist = accountService.getUserById(journalistId);
        if (null == journalist) {
            throw new NewsException("No se ha encontrado la cuenta.");
        }
        if (journalist.getAccountType() == Roles.USER) {
            throw new NewsException("La cuenta pertenece a un usuario, este no puede publicar noticias.");
        }
        news.setCreator(journalist);
    }

    @Transactional(readOnly = true)
    public News getNewsById(String id) throws NewsException {
        validateId(id);
        Optional<News> optNews = newsRepository.findById(id);
        if (!optNews.isPresent()) {
            throw new NewsException("No se han encontrado noticias.");
        }
        return optNews.get();
    }

    @Transactional(readOnly = true)
    public List<News> getAllNews() {
        List<News> newsList = newsRepository.getAllNews();
        return newsList;
    }

    @Transactional
    public void modifyNews(String id, String title,
            String body, MultipartFile photo) throws NewsException {
        validateData(title, body, photo);

        News news = getNewsById(id);
        news.setTitle(title);
        news.setBody(body);
        news.setReleaseDate(new Date(System.currentTimeMillis()));
        news.setImage(imageService.save(photo));
        newsRepository.save(news);

    }

    @Transactional
    //Soft delete
    public void deleteNews(String id) throws NewsException {
        News news = getNewsById(id);
        news.setDeleted(true);
        newsRepository.save(news);
    }

    private void validateData(String title, String body,
            MultipartFile photo) throws NewsException {
        if (null == title || title.isEmpty()) {
            throw new NewsException("El título no es válido.");
        }
        if (null == body || body.isEmpty()) {
            throw new NewsException("No se ha encontrado el cuerpo de la noticia.");
        }
        if (null == photo || photo.isEmpty()) {
            throw new NewsException("La imágen no es válida.");
        }
    }

    private void validateId(String id) throws NewsException {
        if (null == id || id.isEmpty()) {
            throw new NewsException("El ID no es válido.");
        }
    }
}
