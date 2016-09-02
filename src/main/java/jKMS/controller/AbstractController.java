package jKMS.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;

import jKMS.Kartoffelmarktspiel;

/**
 * Abstract for all controllers so that they all get a kms attribute.
 */
@Controller
@ComponentScan
public abstract class AbstractController {

	@Autowired
	protected Kartoffelmarktspiel kms;
	@Autowired
	protected ReloadableResourceBundleMessageSource messageSource;

}
