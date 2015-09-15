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

package nz.net.orcon.kanban.controllers;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Resource;
import javax.jcr.Node;
import javax.jcr.Session;

import nz.net.orcon.kanban.automation.CacheInvalidationInterface;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.Condition;
import nz.net.orcon.kanban.model.Filter;
import nz.net.orcon.kanban.tools.IdentifierTools;
import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 */
@Controller
@RequestMapping("/board/{boardId}/filters")
public class FilterController {
	
	private static final Logger logger = LoggerFactory.getLogger(FilterController.class);
	
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
		
	@Autowired
	CacheInvalidationInterface cacheInvalidationManager;
	
	@Autowired 
	private ListTools listTools;
		
	@RequestMapping(value = "", method=RequestMethod.POST)
	public @ResponseBody Filter createFilter(@PathVariable String boardId,
										  @RequestBody Filter filter) throws Exception {
		if( filter.getPath()!=null ){
			logger.warn("Attempt to update filter using POST");
			throw new Exception("Attempt to Update Filter using POST. Use PUT instead");
		}
			
		ObjectContentManager ocm = ocmFactory.getOcm();
		listTools.ensurePresence(String.format(URI.BOARD_URI, boardId), "filters", ocm.getSession());			
		String newId = IdentifierTools.getIdFromNamedModelClass(filter);
		filter.setPath(String.format(URI.FILTER_URI, boardId, newId.toString()));
		ocm.insert(filter);			
		ocm.save();
		ocm.logout();
		this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);
		return filter;
	}

	@RequestMapping(value = "/{filterId}", method=RequestMethod.PUT)
	public @ResponseBody Filter updateFilter(@PathVariable String boardId,
										  @PathVariable String filterId,
										  @RequestBody Filter filter) throws Exception {
		
		if( filter.getPath()==null ){
			filter.setPath(String.format(URI.FILTER_URI, boardId, filterId));			
		}

		ObjectContentManager ocm = ocmFactory.getOcm();
		ocm.update(filter);
		ocm.save();
		ocm.logout();
		this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);
		return filter;
	}

	@RequestMapping(value = "/{filterId}", method=RequestMethod.GET)
	public @ResponseBody Filter getFilter(@PathVariable String boardId, 
			@PathVariable String filterId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();	
		Filter filter = (Filter) ocm.getObject(Filter.class,String.format(URI.FILTER_URI, boardId, filterId));
		ocm.logout();
		if(filter==null){
			throw new ResourceNotFoundException();
		}		
		return filter;		
	}
	
	@RequestMapping(value = "/{filterId}/conditions", method=RequestMethod.POST)
	public @ResponseBody Condition saveFilterField(@PathVariable String boardId, 
			@PathVariable String filterId,
			@RequestBody Condition filterField) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();	
		listTools.ensurePresence(String.format(URI.FILTER_URI, boardId, filterId), "conditions", ocm.getSession());		
		filterField.setPath(String.format(URI.FILTER_CONDITION_URI, boardId, filterId, filterField.getFieldName()));				
		ocm.insert(filterField);			
		ocm.save();
		ocm.logout();
		this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);
		return filterField;
	}
	
	@RequestMapping(value = "/{filterId}/conditions/{fieldId}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteFilterField(@PathVariable String boardId, 
			@PathVariable String filterId,
			@PathVariable String fieldId) throws Exception {

		ObjectContentManager ocm = ocmFactory.getOcm();	
		String path = String.format(URI.FILTER_CONDITION_URI, boardId, filterId, fieldId);		
		ocm.getSession().removeItem(path);
		ocm.save();
		ocm.logout();
		this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);
	}
	
	@RequestMapping(value = "", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> listFilters(@PathVariable String boardId) throws Exception {
		
		Session session = ocmFactory.getOcm().getSession();
		Map<String,String> result = listTools.list(String.format(URI.FILTER_URI, boardId,""), "name", session);
		session.logout();
		return result;			
	}
	
	@RequestMapping(value = "/{filterId}/execute", method=RequestMethod.GET)
	public @ResponseBody Collection<Card> executeFilter(@PathVariable String boardId, 
														@PathVariable String filterId) 
														throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		
		Collection<Card> cards = listTools.query(boardId, null, filterId, ocm);		
		ocm.logout();
		return cards;		
	}	
	
	@RequestMapping(value = "/{filterId}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteFilter(@PathVariable String boardId, 
			@PathVariable String filterId) throws Exception {
		
		if( StringUtils.isBlank(filterId)){
			return;
		}
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		Node node = ocm.getSession().getNode(String.format(URI.FILTER_URI, boardId, filterId));
		
		if(node==null){
			ocm.logout();
			throw new ResourceNotFoundException();
		}
		
		node.remove();
		ocm.save();
		ocm.logout();
		this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);
	}

}
