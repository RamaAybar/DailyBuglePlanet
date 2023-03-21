/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dailybugleplanet.DailyBuglePlanet.services;

import com.dailybugleplanet.DailyBuglePlanet.entities.Image;
import com.dailybugleplanet.DailyBuglePlanet.exceptions.NewsException;
import java.io.IOException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.dailybugleplanet.DailyBuglePlanet.repositories.ImageRepository;

// @author Ramiro Aybar
@Service
public class ImageService {

    @Autowired
    private ImageRepository repository;

    @Transactional
    public Image save(MultipartFile file) throws NewsException {
        validate(file);
        try {
            Image image = new Image();
            image.setContent(file.getBytes());
            image.setMime(file.getContentType());
            image.setName(file.getName());
            return repository.save(image);

        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
            return null;
        }
    }

    @Transactional
    public Image update(MultipartFile file, String id) throws NewsException {
        validate(file);

        try {
            Image image = getImage(id);
            image.setContent(file.getBytes());
            image.setMime(file.getContentType());
            image.setName(file.getName());
            return repository.save(image);

        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
            return null;
        }
    }

    private void validate(MultipartFile file) throws NewsException {
        if (null == file) {
            throw new NewsException("La imágen no válida.");
        }
    }

    @Transactional(readOnly = true)
    public Image getImage(String id) throws NewsException {
        if (null == id || id.isEmpty()) {
            throw new NewsException("El ID no es válido.");
        }

        Optional<Image> image = repository.findById(id);
        if (!image.isPresent()) {
            throw new NewsException("No se ha encontrado la imágen.");
        }
        return image.get();
    }
}
