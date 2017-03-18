/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.commands.SetupForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.validation.Valid;

/**
 * Created Mario Estrella on 3/17/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Controller
@RequestMapping("/setup")
@SessionAttributes("setupForm")
public class SetupController {
    private static final Logger LOG = LoggerFactory.getLogger(SetupController.class);
    private Validator setupFormValidator;

    @Autowired
    @Qualifier("setupFormValidator")
    public void setSetupFormValidator(Validator setupFormValidator) {
        this.setupFormValidator = setupFormValidator;
    }

    @RequestMapping
    public String getStartPage(final ModelMap modelMap) {
        modelMap.addAttribute("setupForm", new SetupForm());
        return "setup";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String processPage(final @Valid @ModelAttribute("setupForm") SetupForm setupForm, BindingResult bindingResult) {
        LOG.debug("Current progress \n" + setupForm.toString());
        setupFormValidator.validate(setupForm, bindingResult);

        if (bindingResult.hasErrors()) {
            LOG.debug(bindingResult.toString());
            setupForm.setProgress(setupForm.getPrevious());
        }
        return "setup";
    }
}