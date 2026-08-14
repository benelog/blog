package net.benelog.tomcatbed.test.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

@Service
public class ZipCodeService {

	private ConcurrentMap<String,String> zipCodeToAddress = new ConcurrentHashMap<String,String>();
	public ZipCodeService(){
		zipCodeToAddress.put("121-270" , "서울특별시 마포구 상암동");
	}
	
	public String findAddressByZipCode(String zipCode){
		return zipCodeToAddress.get(zipCode);
	}
}
