package jKMS.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jKMS.Application;
import jKMS.LogicHelper;
import jKMS.exceptionHelper.CreateFolderFailedException;

/**
 * Only one page to handle...
 * @author siegmund42
 *
 */
@Controller
public class LoginController extends AbstractController	{

	/**
	 * Show login page, init Helpers
	 * @return 	Name of template
	 * @throws	CreateFolderFailedException
	 */
	@RequestMapping("/login")
	public String login(Model model) throws CreateFolderFailedException	{
		ControllerHelper.init(messageSource);
		LogicHelper.init(messageSource);
		Application.gui.setReady();
		Application.gui.changeLanguage();
		model.addAttribute("languages", ControllerHelper.getLanguages());
		return "login";
	}
}
