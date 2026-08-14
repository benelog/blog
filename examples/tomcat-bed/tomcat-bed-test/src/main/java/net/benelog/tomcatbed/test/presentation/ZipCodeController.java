package net.benelog.tomcatbed.test.presentation;

import net.benelog.tomcatbed.test.service.ZipCodeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ZipCodeController {
	private ZipCodeService service;

	@Autowired
	public ZipCodeController(ZipCodeService service){
		this.service = service;
	}

	@RequestMapping("/searchAddress")
	public ModelAndView searchAdress(@RequestParam String zipCode){
		String address = service.findAddressByZipCode(zipCode);
		return new ModelAndView("addressDetail").addObject("address", address);
	}
}
