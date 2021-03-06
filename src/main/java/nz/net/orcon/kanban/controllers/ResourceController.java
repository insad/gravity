/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2015 Orcon Limited
 * (C) Copyright 2015 Peter Harrison
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

import java.io.BufferedReader;
import java.util.Map;

import javax.annotation.Resource;
import javax.jcr.Node;
import javax.servlet.http.HttpServletRequest;

import nz.net.orcon.kanban.automation.CacheInvalidationInterface;
import nz.net.orcon.kanban.tools.IdentifierTools;
import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/board/{boardId}/resources")
public class ResourceController {
		
	private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);
	
	private static String RESOURCE = "RESOURCE";
	
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired 
	private ListTools listTools;
	
	@Autowired
	private ResourceCache resourceCache;
	
	@Autowired
	CacheInvalidationInterface cacheInvalidationManager;
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{resourceId}", method=RequestMethod.GET)
	public @ResponseBody String getResource(@PathVariable String boardId, 
			@PathVariable String resourceId) throws Exception {
		
		return resourceCache.getItem(boardId, IdentifierTools.getIdFromName(resourceId));		
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")
	@RequestMapping(value = "/{resourceId}", method=RequestMethod.POST)
	public @ResponseBody void createResource(@PathVariable String boardId, 
			@PathVariable String resourceId, 
			HttpServletRequest request) throws Exception {
				
		logger.info("Saving Resource " + boardId + "/" + resourceId);
		BufferedReader reader = request.getReader();
		reader.reset();
		StringBuilder valueBuilder = new StringBuilder();
		
		while(true){
			String newValue = reader.readLine();
			if( newValue!=null){
				valueBuilder.append(newValue);
				valueBuilder.append("\n");
			} else {
				break;
			}
		}
		
		String value = valueBuilder.toString();
		
		logger.info("Resource Text: " + value);
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		
		try {
		
			listTools.ensurePresence(String.format( URI.BOARD_URI, boardId), "resources", ocm.getSession());
			
			Node node = ocm.getSession().getNode(String.format(URI.RESOURCE_URI, boardId, ""));
			Node addNode = node.addNode(resourceId);
			
			addNode.setProperty("resource",value);
			addNode.setProperty("name",resourceId);
			ocm.getSession().save();
			this.cacheInvalidationManager.invalidate(RESOURCE, boardId,resourceId);
		} finally {
			ocm.logout();
		}
		
		logger.info("Resource Saved " + resourceId);
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")	
	@RequestMapping(value = "/{resourceId}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteResource(@PathVariable String boardId, 
			@PathVariable String resourceId) throws Exception {
		
		logger.info("Deleting Resource " + resourceId);
		ObjectContentManager ocm = ocmFactory.getOcm();
		try {
			Node node = ocm.getSession().getNode(String.format(URI.RESOURCE_URI, boardId, resourceId));
			if(node==null){
				ocm.logout();
				throw new ResourceNotFoundException();
			}
			node.remove();
			ocm.save();
			this.cacheInvalidationManager.invalidate(RESOURCE, boardId,resourceId);
		} finally {
			ocm.logout();
		}
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")	
	@RequestMapping(value = "", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> listResources(@PathVariable String boardId) throws Exception {	
		logger.info("Getting Resource List");
		return this.resourceCache.list(boardId,"");		
	}
	
}
