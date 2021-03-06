/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2015 Orcon Limited
 * 
 * This file is part of Gravity Workflow Automation.
 *
 * Gravity Workflow Automation is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Gravity Workflow Automation is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *    
 * You should have received a copy of the GNU General Public License
 * along with Gravity Workflow Automation.  
 * If not, see <http://www.gnu.org/licenses/>. 
 */

package nz.net.orcon.kanban.automation.plugin;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import nz.net.orcon.kanban.controllers.CardController;
import nz.net.orcon.kanban.model.Action;

/**
 * This Plugin moves a card from the existing phase to the phase specified in the resource.
 * 
 * @author peter
 */
public class MovePlugin implements Plugin {

	@Autowired
	CardController controller;
	
	@Override
	public Map<String, Object> process(Action action,
			Map<String, Object> context) throws Exception {
		
		String boardId = (String) context.get("boardid");
		String phaseId = (String) context.get("phaseid");
		String cardId = (String) context.get("cardid");

		controller.moveCard(boardId, phaseId, cardId, action.getResource());
		context.put("phaseid",  action.getResource());
		return context;
	}

}
