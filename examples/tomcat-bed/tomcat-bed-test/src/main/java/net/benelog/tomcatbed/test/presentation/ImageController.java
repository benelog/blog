package net.benelog.tomcatbed.test.presentation;

import net.benelog.tomcatbed.domain.Image;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ImageController {
	@RequestMapping("/viewImage/{imageId}")
	@ResponseBody
	public Image view(@PathVariable String imageId){
		String imagePath = String.format("/img/%s.png",imageId);
		Image image = new Image();
		image.setHeight(64);
		image.setWidth(64);
		image.setSrc(imagePath);
		
		return image;
	}
}
