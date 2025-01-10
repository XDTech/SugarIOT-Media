package org.sugar.media.controller.opencv;

import jakarta.annotation.Resource;
import org.opencv.core.Mat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.sugar.media.service.opencv.OpencvService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Date:2025/01/08 17:50:34
 * Author：Tobin
 * Description:
 */

@RestController
@RequestMapping("/ai")
public class AIController {



}
