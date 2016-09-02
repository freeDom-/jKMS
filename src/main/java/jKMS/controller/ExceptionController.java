package jKMS.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import jKMS.LogicHelper;
import jKMS.exceptionHelper.CreateFolderFailedException;
import jKMS.exceptionHelper.InvalidStateChangeException;

/**
 * Controller for handling uncaught Exceptions
 * @author siegmund42
 *
 */
@ControllerAdvice
public class ExceptionController {
	
	/**
	 * Catch Excpetions that are thrown from the Logic functions 
	 * if the user is not in the right state
	 * @param 	e	InvalidStateChangeException
	 * @return		ModelAndView with standardException Template
	 */
	@ExceptionHandler
	public ModelAndView handleIllegalStateException(IllegalStateException e) {
		e.printStackTrace();
		ModelAndView mav = new ModelAndView();
		mav.setViewName("standardException");
		mav.addObject("error", LogicHelper.getLocalizedMessage("error.state.error"));
		mav.addObject("message", LogicHelper.getLocalizedMessage("error.state.message"));
		return mav;
	}
	
	/**
	 * If user jumped through sites not by clicking our links
	 * @param 	e	InvalidStateChangeException
	 * @return		ModelAndView with standardException Template
	 */
	@ExceptionHandler
	public ModelAndView handleInavlidStateChangeException(InvalidStateChangeException e) {
		e.printStackTrace();
		ModelAndView mav = new ModelAndView();
		mav.setViewName("standardException");
		mav.addObject("error", LogicHelper.getLocalizedMessage("error.state.error"));
		mav.addObject("message", LogicHelper.getLocalizedMessage("error.state.message"));
		return mav;
	}
	
	/**
	 * Handles error when creating folders
	 * @param 	e	CreateFolderFailedException
	 * @return		ModelAndView with standardException Template
	 */
	@ExceptionHandler
	public ModelAndView handleCreateFolderFailedException(CreateFolderFailedException e) {
		e.printStackTrace();
		ModelAndView mav = new ModelAndView();
		mav.setViewName("standardException");
		mav.addObject("message", LogicHelper.getLocalizedMessage("error.buildFolderStructure.message"));
		mav.addObject("error", LogicHelper.getLocalizedMessage("error.buildFolderStructure.error"));
		return mav;
	}

}