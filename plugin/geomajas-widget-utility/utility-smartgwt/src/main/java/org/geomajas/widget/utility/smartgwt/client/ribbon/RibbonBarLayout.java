/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2011 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */

package org.geomajas.widget.utility.smartgwt.client.ribbon;

import java.util.ArrayList;
import java.util.List;

import org.geomajas.command.dto.GetClientUserDataRequest;
import org.geomajas.command.dto.GetClientUserDataResponse;
import org.geomajas.gwt.client.command.CommandCallback;
import org.geomajas.gwt.client.command.GwtCommand;
import org.geomajas.gwt.client.command.GwtCommandDispatcher;
import org.geomajas.gwt.client.widget.MapWidget;
import org.geomajas.widget.utility.client.ribbon.RibbonBar;
import org.geomajas.widget.utility.client.ribbon.RibbonColumn;
import org.geomajas.widget.utility.client.ribbon.RibbonGroup;
import org.geomajas.widget.utility.server.configuration.RibbonBarInfo;
import org.geomajas.widget.utility.server.configuration.RibbonColumnInfo;
import org.geomajas.widget.utility.server.configuration.RibbonGroupInfo;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.HLayout;

/**
 * SmartGWT implementation of the {@link RibbonBar} interface. This widget is based upon a VLayout.
 * 
 * @author Pieter De Graef
 */
public class RibbonBarLayout extends HLayout implements RibbonBar {

	private static final String RIBBON_BAR_INFO_CLASS =
			"org.geomajas.widget.utility.server.configuration.RibbonBarInfo";

	private boolean showGroupTitles = true;

	private List<RibbonGroup> groups = new ArrayList<RibbonGroup>();

	// ------------------------------------------------------------------------
	// Constructors:
	// ------------------------------------------------------------------------

	/** Initialize an empty ribbon bar widget. */
	public RibbonBarLayout() {
		setMembersMargin(2);
		setStyleName("ribbon");
	}

	/**
	 * Create a ribbon bar widget using the given configuration information and a map.
	 * 
	 * @param barInfo
	 *            The information from which to build the ribbon bar. In order to create the correct sub-widgets, the
	 *            {@link RibbonColumnRegistry} will be used.
	 * @param mapWidget
	 *            The map widget onto which many actions in this ribbon apply.
	 */
	public RibbonBarLayout(RibbonBarInfo barInfo, MapWidget mapWidget) {
		this();
		buildGui(barInfo, mapWidget);
	}

	/**
	 * Create a ribbon bar widget using a back-end spring bean identifier and a map.
	 * 
	 * @param mapWidget
	 *            The map widget onto which many actions in this ribbon apply.
	 * @param beanId
	 *            A unique spring bean identifier for a bean of class {@link RibbonBarInfo}. This configuration is then
	 *            fetched and applied.
	 */
	public RibbonBarLayout(final MapWidget mapWidget, String beanId) {
		this();

		GetClientUserDataRequest request = new GetClientUserDataRequest();
		request.setIdentifier(beanId);
		request.setClassName(RIBBON_BAR_INFO_CLASS);
		GwtCommand command = new GwtCommand(GetClientUserDataRequest.COMMAND);
		command.setCommandRequest(request);
		GwtCommandDispatcher.getInstance().execute(command, new CommandCallback<GetClientUserDataResponse>() {

			public void execute(GetClientUserDataResponse response) {
				if (response.getInformation() instanceof RibbonBarInfo) {
					buildGui((RibbonBarInfo) response.getInformation(), mapWidget);
				} else {
					// throw some exception??
				}
			}
		});
	}

	// ------------------------------------------------------------------------
	// Ribbon implementation:
	// ------------------------------------------------------------------------

	public void addGroup(RibbonGroup ribbonGroup) {
		if (ribbonGroup == null) {
			throw new NullPointerException("Cannot add RibbonGroup with null value.");
		}
		groups.add(ribbonGroup);
		ribbonGroup.setShowTitle(showGroupTitles);
		ribbonGroup.asWidget().setStyleName(getStyleName() + "Group");
		addMember(ribbonGroup.asWidget());
	}

	public void addGroup(RibbonGroup ribbonGroup, int index) {
		if (ribbonGroup == null) {
			throw new NullPointerException("Cannot add RibbonGroup with null value.");
		}
		groups.add(index, ribbonGroup);
		ribbonGroup.setShowTitle(showGroupTitles);
		ribbonGroup.asWidget().setStyleName(getStyleName() + "Group");
		addMember(ribbonGroup.asWidget(), index);
	}

	public void removeGroup(RibbonGroup ribbonGroup) {
		if (ribbonGroup == null) {
			throw new NullPointerException("Cannot remove RibbonGroup with null value.");
		}
		groups.remove(ribbonGroup);
		removeMember((Canvas) ribbonGroup.asWidget());
	}

	public void removeGroup(int index) {
		removeGroup(getGroup(index));
	}

	public RibbonGroup getGroup(int index) {
		return groups.get(index);
	}

	public void setShowGroupTitles(boolean showGroupTitles) {
		this.showGroupTitles = showGroupTitles;
		for (RibbonGroup group : groups) {
			group.setShowTitle(showGroupTitles);
		}
	}

	public boolean isShowGroupTitles() {
		return showGroupTitles;
	}

	// ------------------------------------------------------------------------
	// SmartGWT method overrides:
	// ------------------------------------------------------------------------

	@Override
	public void setStyleName(String styleName) {
		super.setStyleName(styleName);
		for (RibbonGroup group : groups) {
			group.asWidget().setStyleName(getStyleName() + "Group");
		}
	}

	// ------------------------------------------------------------------------
	// Private methods:
	// ------------------------------------------------------------------------

	private void buildGui(RibbonBarInfo barInfo, MapWidget mapWidget) {
		for (RibbonGroupInfo groupInfo : barInfo.getGroups()) {
			RibbonGroupLayout group = new RibbonGroupLayout(groupInfo.getTitle());
			for (RibbonColumnInfo columnInfo : groupInfo.getColumns()) {
				RibbonColumn ribbonColumn = RibbonColumnRegistry.getRibbonColumn(columnInfo.getType(),
						columnInfo.getTools(), columnInfo.getParameters(), mapWidget);
				if (ribbonColumn != null) {
					group.addColumn(ribbonColumn);
				}
			}
			addGroup(group);
		}
	}
}