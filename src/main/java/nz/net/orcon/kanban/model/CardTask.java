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

package nz.net.orcon.kanban.model;

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

@Node
public class CardTask extends CardEvent {

	private static final long serialVersionUID = -1628596616167689718L;

	@Field
	private String taskid;

	@Field
	private Boolean complete;

	public void setTaskid(String taskid) {
		this.taskid = taskid;
	}

	public String getTaskid() {
		return taskid;
	}

	public void setComplete(Boolean complete) {
		this.complete = complete;
	}

	public Boolean getComplete() {
		return complete;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		CardTask other = (CardTask) obj;		
		return (this.getTaskid().equals(other.getTaskid()));
		
	}	
	@Override
	public int hashCode() {
		return 19 + (this.getTaskid() != null ? this.getTaskid().hashCode() : 0);		
	}
}
