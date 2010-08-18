/*
 * This file is part of Geomajas, a component framework for building
 * rich Internet applications (RIA) with sophisticated capabilities for the
 * display, analysis and management of geographic information.
 * It is a building block that allows developers to add maps
 * and other geographic data capabilities to their web applications.
 *
 * Copyright 2008-2010 Geosparc, http://www.geosparc.com, Belgium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.geomajas.gwt.client.widget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geomajas.command.CommandResponse;
import org.geomajas.command.dto.GetMapConfigurationRequest;
import org.geomajas.command.dto.GetMapConfigurationResponse;
import org.geomajas.configuration.client.ClientMapInfo;
import org.geomajas.geometry.Coordinate;
import org.geomajas.global.Api;
import org.geomajas.gwt.client.action.menu.AboutAction;
import org.geomajas.gwt.client.command.CommandCallback;
import org.geomajas.gwt.client.command.GwtCommand;
import org.geomajas.gwt.client.command.GwtCommandDispatcher;
import org.geomajas.gwt.client.controller.GraphicsController;
import org.geomajas.gwt.client.controller.PanController;
import org.geomajas.gwt.client.gfx.GraphicsContext;
import org.geomajas.gwt.client.gfx.ImageContext;
import org.geomajas.gwt.client.gfx.MenuContext;
import org.geomajas.gwt.client.gfx.Paintable;
import org.geomajas.gwt.client.gfx.PaintableGroup;
import org.geomajas.gwt.client.gfx.Painter;
import org.geomajas.gwt.client.gfx.PainterVisitor;
import org.geomajas.gwt.client.gfx.WorldPaintable;
import org.geomajas.gwt.client.gfx.paintable.Composite;
import org.geomajas.gwt.client.gfx.paintable.mapaddon.MapAddon;
import org.geomajas.gwt.client.gfx.paintable.mapaddon.PanButtonCollection;
import org.geomajas.gwt.client.gfx.paintable.mapaddon.ScaleBar;
import org.geomajas.gwt.client.gfx.paintable.mapaddon.Watermark;
import org.geomajas.gwt.client.gfx.paintable.mapaddon.ZoomAddon;
import org.geomajas.gwt.client.gfx.paintable.mapaddon.ZoomToRectangleAddon;
import org.geomajas.gwt.client.gfx.painter.CirclePainter;
import org.geomajas.gwt.client.gfx.painter.FeaturePainter;
import org.geomajas.gwt.client.gfx.painter.FeatureTransactionPainter;
import org.geomajas.gwt.client.gfx.painter.GeometryPainter;
import org.geomajas.gwt.client.gfx.painter.ImagePainter;
import org.geomajas.gwt.client.gfx.painter.MapModelPainter;
import org.geomajas.gwt.client.gfx.painter.RasterLayerPainter;
import org.geomajas.gwt.client.gfx.painter.RasterTilePainter;
import org.geomajas.gwt.client.gfx.painter.RectanglePainter;
import org.geomajas.gwt.client.gfx.painter.TextPainter;
import org.geomajas.gwt.client.gfx.painter.VectorLayerPainter;
import org.geomajas.gwt.client.gfx.painter.VectorTilePainter;
import org.geomajas.gwt.client.gfx.style.ShapeStyle;
import org.geomajas.gwt.client.map.MapModel;
import org.geomajas.gwt.client.map.MapView;
import org.geomajas.gwt.client.map.event.EditingEvent;
import org.geomajas.gwt.client.map.event.EditingEvent.EditingEventType;
import org.geomajas.gwt.client.map.event.EditingHandler;
import org.geomajas.gwt.client.map.event.FeatureDeselectedEvent;
import org.geomajas.gwt.client.map.event.FeatureSelectedEvent;
import org.geomajas.gwt.client.map.event.FeatureSelectionHandler;
import org.geomajas.gwt.client.map.event.LayerChangedHandler;
import org.geomajas.gwt.client.map.event.LayerLabeledEvent;
import org.geomajas.gwt.client.map.event.LayerShownEvent;
import org.geomajas.gwt.client.map.event.MapModelEvent;
import org.geomajas.gwt.client.map.event.MapModelHandler;
import org.geomajas.gwt.client.map.event.MapViewChangedEvent;
import org.geomajas.gwt.client.map.event.MapViewChangedHandler;
import org.geomajas.gwt.client.map.feature.Feature;
import org.geomajas.gwt.client.map.feature.FeatureTransaction;
import org.geomajas.gwt.client.map.layer.Layer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Cursor;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ResizedEvent;
import com.smartgwt.client.widgets.events.ResizedHandler;
import com.smartgwt.client.widgets.menu.Menu;

/**
 * <p>
 * The most important widget in this framework. This is the central map. It is built using a model-view-controller
 * paradigm, where this widget basically holds the 3 components in one. The model is represented by the {@link MapModel}
 * class, the view is represented by the graphics contexts (check the {@link org.geomajas.gwt.client.gfx.MapContext}
 * class), and the controller by a {@link GraphicsController}.
 * </p>
 * <p>
 * This widget will initialize itself automatically on the onDraw event. The initialization means that it will fetch the
 * correct XML configuration (using the map's ID and the applicationID - see constructor), and build the MapModel. From
 * that point on, rendering is possible.
 * </p>
 * 
 * @author Pieter De Graef
 * @author Oliver May
 * @since 1.6.0
 */
@Api
public class MapWidget extends Canvas implements MapViewChangedHandler, MapModelHandler {

	// Private fields regarding internal workings:

	private MapModel mapModel;

	private GraphicsWidget graphics;

	private PainterVisitor painterVisitor;

	private double unitLength;

	private double pixelLength;

	private HandlerRegistration mouseWheelRegistration;

	private Menu defaultMenu;

	protected String applicationId;

	// MapWidget options:

	protected boolean navigationAddonEnabled = true;

	protected boolean scaleBarEnabled = true;

	/** Is zooming in and out using the mouse wheel enabled or not? This is true by default. */
	private boolean zoomOnScrollEnabled;

	private boolean resizedHandlerDisabled;

	// Rendering related fields:

	private PaintableGroup rasterGroup = new Composite("raster");

	private PaintableGroup vectorGroup = new Composite("vector");

	private PaintableGroup worldGroup = new Composite("world");

	private PaintableGroup screenGroup = new Composite("screen");

	private Map<String, WorldPaintable> worldPaintables = new HashMap<String, WorldPaintable>();

	private Map<String, MapAddon> addons = new HashMap<String, MapAddon>();

	/**
	 * Map groups: rendering should be done in one of these. Try to always use either the SCREEN or the WORLD group,
	 * unless you need something very specific and you know what you are doing.
	 */
	public enum RenderGroup {
		/**
		 * The pan group. Drawing should be done in pan coordinates. All raster layers are drawn in pan coordinates. In
		 * essence this means that the coordinates are expected to have been scaled for the current scale before
		 * drawing, and that only the translation still needs to occur. For advanced use only.
		 */
		RASTER,

		/**
		 * The pan group. Drawing should be done in pan coordinates. All vector layers, their selection and their labels
		 * are drawn in pan coordinates. In essence this means that the coordinates are expected to have been scaled for
		 * the current scale before drawing, and that only the translation still needs to occur. For advanced use only.
		 */
		VECTOR,

		/**
		 * The world group. Drawing should be done in world coordinates. World coordinates means that the map coordinate
		 * system is used. The advantage of rendering objects in the world group, is that when the user moves the map
		 * around, the objects will move with it.
		 */
		WORLD,

		/**
		 * The screen group. Drawing should be done in screen coordinates. Screen coordinates are expressed in pixels,
		 * starting from the top left corner of the map. When rendering objects in the screen group they will always
		 * appear at a fixed position, even when the user moves the map about.
		 */
		SCREEN
	}

	/**
	 * Rendering statuses.
	 */
	public enum RenderStatus {
		/**
		 * Render paintable, including all children.
		 */
		ALL,

		/**
		 * Redraw paintable, parent only.
		 */
		UPDATE,

		/**
		 * Delete the paintable.
		 */
		DELETE
	}

	/**
	 * Types of scroll on zoom.
	 */
	public static enum ScrollZoomType {
		/**
		 * When scroll zooming, retain the center of the map position.
		 */
		ZOOM_CENTER,
		/**
		 * When scroll zooming, retain the mouse position.
		 */
		ZOOM_POSITION
	}

	// -------------------------------------------------------------------------
	// Constructor:
	// -------------------------------------------------------------------------

	/**
	 * <p>
	 * Initialize a map with a unique ID that can be retrieved in the server side configuration, and an additional
	 * application ID that too can be retrieved in the server side configuration. When the onDraw event occurs, a map
	 * will automatically initialize itself.
	 * </p>
	 * <p>
	 * This constructor will register a whole list of default painters, set a default context menu and apply a zoom
	 * controller that reacts on mouse wheel events.
	 * </p>
	 * 
	 * @param id
	 *            The map's unique identifier, retrievable in the XML configuration.
	 * @param applicationId
	 *            The identifier of the application to which this map belongs.
	 * @since 1.6.0
	 */
	@Api
	public MapWidget(String id, String applicationId) {
		super(id);
		this.applicationId = applicationId;
		mapModel = new MapModel(getID() + "Graphics");
		mapModel.addMapModelHandler(this);
		mapModel.getMapView().addMapViewChangedHandler(this);
		graphics = new GraphicsWidget(this, getID() + "Graphics");
		painterVisitor = new PainterVisitor(graphics);
		mapModel.addFeatureSelectionHandler(new MapWidgetFeatureSelectionHandler(this));
		graphics.setFallbackController(new PanController(this));

		// Painter registration:
		painterVisitor.registerPainter(new CirclePainter());
		painterVisitor.registerPainter(new RectanglePainter());
		painterVisitor.registerPainter(new TextPainter());
		painterVisitor.registerPainter(new GeometryPainter());
		painterVisitor.registerPainter(new ImagePainter());
		painterVisitor.registerPainter(new MapModelPainter(this));
		painterVisitor.registerPainter(new RasterLayerPainter(this));
		painterVisitor.registerPainter(new RasterTilePainter());
		painterVisitor.registerPainter(new VectorLayerPainter(this));
		painterVisitor.registerPainter(new VectorTilePainter());
		painterVisitor.registerPainter(new FeatureTransactionPainter(this));

		// Install a default menu:
		defaultMenu = new Menu();
		defaultMenu.addItem(new AboutAction());
		setContextMenu(defaultMenu);

		// Resizing should work correctly!
		setWidth100();
		setHeight100();
		setDynamicContents(true);
		addResizedHandler(new MapResizedHandler(this));
		setZoomOnScrollEnabled(true);

		mapModel.getFeatureEditor().addEditingHandler(new EditingHandler() {

			public void onEditingChange(EditingEvent event) {
				FeatureTransaction ft = mapModel.getFeatureEditor().getFeatureTransaction();
				if (ft != null) {
					if (event.getEditingEventType().equals(EditingEventType.START_EDITING)) {
						render(ft, RenderGroup.VECTOR, RenderStatus.ALL);
					} else if (event.getEditingEventType().equals(EditingEventType.STOP_EDITING)) {
						render(ft, RenderGroup.VECTOR, RenderStatus.DELETE);
					}
				}
			}
		});
	}

	// -------------------------------------------------------------------------
	// Initialization:
	// -------------------------------------------------------------------------

	/**
	 * <p>
	 * The map's initialization method. This method is triggered automatically on the "onDraw" event. So in normal cases
	 * you should never have to call it. Nevertheless, there are a few cases where the onDraw event might never come, so
	 * it is still possible to initialize the map directly.
	 * </p>
	 * <p>
	 * Know that without this method, the map would be an empty shell. This method will ask the server for the correct
	 * configuration, so that it is possible to build a model (MapModel).
	 * </p>
	 * 
	 * @since 1.6.0
	 */
	@Api
	public void init() {
		GwtCommand commandRequest = new GwtCommand("command.configuration.GetMap");
		commandRequest.setCommandRequest(new GetMapConfigurationRequest(id, applicationId));
		GwtCommandDispatcher.getInstance().execute(commandRequest, new CommandCallback() {

			public void execute(CommandResponse response) {
				if (response instanceof GetMapConfigurationResponse) {
					GetMapConfigurationResponse r = (GetMapConfigurationResponse) response;
					initializationCallback(r);
				}
			}
		});
	}

	// -------------------------------------------------------------------------
	// Rendering related methods:
	// -------------------------------------------------------------------------

	/**
	 * Return the Object that represents one the default RenderGroups in the DOM tree when drawing.
	 * 
	 * @param group
	 *            The general group definition (RenderGroup.SCREEN, RenderGroup.WORLD, ...)
	 * @return paintable group
	 */
	public PaintableGroup getGroup(RenderGroup group) {
		switch (group) {
			case RASTER:
				return rasterGroup;
			case VECTOR:
				return vectorGroup;
			case WORLD:
				return worldGroup;
			case SCREEN:
			default:
				return screenGroup;
		}
	}

	/**
	 * The main rendering method. Renders some paintable object in the given group, using the given status.
	 * 
	 * @param paintable
	 *            The actual object to be rendered. Should always contain location and styling information.
	 * @param renderGroup
	 *            In what group to render the paintable object?
	 * @param status
	 *            how to render
	 * @since 1.6.0
	 */
	@Api
	public void render(Paintable paintable, RenderGroup renderGroup, RenderStatus status) {
		PaintableGroup group = null;
		if (renderGroup != null) {
			group = getGroup(renderGroup);
		}
		if (!graphics.isAttached()) {
			return;
		}
		if (paintable == null) {
			paintable = this.mapModel;
		}
		if (RenderStatus.DELETE.equals(status)) {
			List<Painter> painters = painterVisitor.getPaintersForObject(paintable);
			if (painters != null) {
				for (Painter painter : painters) {
					painter.deleteShape(paintable, group, graphics);
				}
			}
		} else {
			if (RenderStatus.ALL.equals(status)) {
				if (paintable == this.mapModel) {
					// Paint the world space paintable objects:
					for (WorldPaintable worldPaintable : worldPaintables.values()) {
						worldPaintable.transform(mapModel.getMapView().getWorldViewTransformer());
						worldPaintable.accept(painterVisitor, getGroup(RenderGroup.WORLD), mapModel.getMapView()
								.getBounds(), true);
					}
				}
				paintable.accept(painterVisitor, group, mapModel.getMapView().getBounds(), true);
			} else if (RenderStatus.UPDATE.equals(status)) {
				if (paintable == this.mapModel) {
					// Paint the world space paintable objects:
					for (WorldPaintable worldPaintable : worldPaintables.values()) {
						worldPaintable.transform(mapModel.getMapView().getWorldViewTransformer());
						worldPaintable.accept(painterVisitor, getGroup(RenderGroup.WORLD), mapModel.getMapView()
								.getBounds(), false);
					}
				}
				paintable.accept(painterVisitor, group, mapModel.getMapView().getBounds(), false);
			}
		}
	}

	/**
	 * Apply a new cursor on the map.
	 * 
	 * @param cursor
	 *            The new cursor to be used when the mouse hovers over the map.
	 */
	public void setCursor(Cursor cursor) {
		super.setCursor(cursor);
		graphics.getRasterContext().setCursor(null, cursor.getValue());
		graphics.getVectorContext().setCursor(null, cursor.getValue());
	}

	/**
	 * Apply a new context menu on the map.
	 * 
	 * @param contextMenu
	 *            The new context menu to be used when the user right clicks on the map.
	 */
	public void setContextMenu(Menu contextMenu) {
		if (null == contextMenu) {
			super.setContextMenu(defaultMenu);
		} else {
			super.setContextMenu(contextMenu);
		}
	}

	// -------------------------------------------------------------------------
	// Registration of functional objects:
	// -------------------------------------------------------------------------

	/**
	 * Register a new painter. A painter is responsible for painting <code>Paintable</code> objects of a certain class.
	 * 
	 * @param painter
	 *            The new painter to be registered. If that painter is already in the list, nothing will happen.
	 * @since 1.6.0
	 */
	@Api
	public void registerPainter(Painter painter) {
		painterVisitor.registerPainter(painter);
	}

	/**
	 * Remove a certain painter from the list again, so that it is no longer used.
	 * 
	 * @param painter
	 *            The registered painter to be removed. If it can't be found in the list, nothing will happen.
	 * @since 1.6.0
	 */
	@Api
	public void unregisterPainter(Painter painter) {
		painterVisitor.unregisterPainter(painter);
	}

	/**
	 * Register a certain <code>MapAddon</code> to be placed on the map. Map add-ons are fixed position entities on a
	 * map with possibly additional functionality. Examples are the scale bar, and navigation buttons.
	 * 
	 * @param addon
	 *            The new add-on to be added to the map.
	 */
	public void registerMapAddon(MapAddon addon) {
		if (addon != null && !addons.containsKey(addon.getId())) {
			addons.put(addon.getId(), addon);
			addon.setMapSize(getWidth(), getHeight());
			render(addon, RenderGroup.SCREEN, RenderStatus.ALL);
			addon.onDraw();
		}
	}

	/**
	 * Remove a registered map add-on from the map. Map add-ons are fixed position entities on a map with possibly
	 * additional functionality. Examples are the scale bar, and navigation buttons.
	 * 
	 * @param addon
	 *            The add-on to be removed from the map. If it can't be found, nothing happens.
	 */
	public void unregisterMapAddon(MapAddon addon) {
		if (addon != null && addons.containsKey(addon.getId())) {
			addons.remove(addon.getId());
			graphics.getVectorContext().deleteGroup(addon);
			addon.onRemove();
		}
	}

	/**
	 * <p>
	 * Register a <code>WorldPaintable</code> object to be painted on the map. By doing so, the object will be painted
	 * immediately on the correct position, and when the user navigates around, the map will automatically make sure the
	 * <code>WorldPaintable</code> object is re-drawn at the correct location.
	 * </p>
	 * <p>
	 * If you want to draw objects in World Space, this would be the way to go.
	 * </p>
	 * 
	 * @param worldPaintable
	 *            The new WorldPaintable object to be rendered on the map.
	 * @since 1.6.0
	 */
	@Api
	public void registerWorldPaintable(WorldPaintable worldPaintable) {
		if (worldPaintable != null && !worldPaintables.containsKey(worldPaintable.getId())) {
			worldPaintables.put(worldPaintable.getId(), worldPaintable);
			worldPaintable.transform(mapModel.getMapView().getWorldViewTransformer());
			render(worldPaintable, RenderGroup.WORLD, RenderStatus.ALL);
		}
	}

	/**
	 * Remove a registered <code>WorldPaintable</code> object from the list and from the map.
	 * 
	 * @param worldPaintable
	 *            The registered WorldPaintable object to be removed again from the map.
	 * @since 1.6.0
	 */
	@Api
	public void unregisterWorldPaintable(WorldPaintable worldPaintable) {
		if (worldPaintable != null && worldPaintables.containsKey(worldPaintable.getId())) {
			render(worldPaintable, RenderGroup.WORLD, RenderStatus.DELETE);
			worldPaintables.remove(worldPaintable.getId());
		}
	}

	// -------------------------------------------------------------------------
	// Applying options:
	// -------------------------------------------------------------------------

	/**
	 * Determines whether or not the zooming using the mouse wheel should be enabled or not.
	 * 
	 * @param zoomOnScrollEnabled
	 *            True or false. Enable or disable zooming using the mouse wheel.
	 */
	public void setZoomOnScrollEnabled(boolean zoomOnScrollEnabled) {
		if (mouseWheelRegistration != null) {
			mouseWheelRegistration.removeHandler();
			mouseWheelRegistration = null;
		}
		this.zoomOnScrollEnabled = zoomOnScrollEnabled;
		if (zoomOnScrollEnabled) {
			mouseWheelRegistration = graphics.addMouseWheelHandler(new ZoomOnScrollController());
		}
	}

	/**
	 * Is the zooming using the mouse wheel currently enabled or not?
	 * 
	 * @return true when zoom on scroll is enabled
	 */
	public boolean isZoomOnScrollEnabled() {
		return zoomOnScrollEnabled;
	}

	/**
	 * Enables or disables the scale bar. This setting has immediate effect on the map.
	 * 
	 * @param enabled
	 *            set status
	 */
	public void setScalebarEnabled(boolean enabled) {
		scaleBarEnabled = enabled;
		final String scaleBarId = "scalebar";

		if (scaleBarEnabled) {
			ScaleBar scalebar = new ScaleBar("scalebar", this);
			scalebar.setVerticalAlignment(VerticalAlignment.BOTTOM);
			scalebar.setHorizontalMargin(2);
			scalebar.setVerticalMargin(2);
			scalebar.initialize(getMapModel().getMapInfo().getDisplayUnitType(), unitLength, new Coordinate(20,
					graphics.getHeight() - 25));
			scalebar.adjustScale(mapModel.getMapView().getCurrentScale());
			registerMapAddon(scalebar);
		} else {
			unregisterMapAddon(addons.get(scaleBarId));
		}
	}

	/**
	 * Is the scale bar (MapAddon) currently enabled/visible or not?
	 * 
	 * @return true when scale bar is enabled
	 */
	public boolean isScaleBarEnabled() {
		return scaleBarEnabled;
	}

	/**
	 * Enables or disables the panning buttons. This setting has immediate effect on the map.
	 * 
	 * @param enabled
	 *            enabled status
	 */
	public void setNavigationAddonEnabled(boolean enabled) {
		navigationAddonEnabled = enabled;
		final String panId = "panBTNCollection";
		final String zoomId = "zoomAddon";
		final String zoomRectId = "zoomRectAddon";

		if (enabled) {
			PanButtonCollection panButtons = new PanButtonCollection(panId, this);
			panButtons.setHorizontalMargin(5);
			panButtons.setVerticalMargin(5);
			registerMapAddon(panButtons);

			ZoomAddon zoomAddon = new ZoomAddon(zoomId, this);
			zoomAddon.setHorizontalMargin(20);
			zoomAddon.setVerticalMargin(65);
			registerMapAddon(zoomAddon);

			ZoomToRectangleAddon zoomToRectangleAddon = new ZoomToRectangleAddon(zoomRectId, this);
			zoomToRectangleAddon.setHorizontalMargin(20);
			zoomToRectangleAddon.setVerticalMargin(135);
			registerMapAddon(zoomToRectangleAddon);
		} else {
			unregisterMapAddon(addons.get(panId));
			unregisterMapAddon(addons.get(zoomId));
			unregisterMapAddon(addons.get(zoomRectId));
		}
	}

	/**
	 * Is the navigation (MapAddon) currently enabled/visible or not?
	 * 
	 * @return true when navigation addon is enabled
	 */
	public boolean isNavigationAddonEnabled() {
		return navigationAddonEnabled;
	}

	/**
	 * Will the map automatically react on resize events or not? This option is turned on be default.
	 * 
	 * @return true when the resize handler is disabled
	 */
	public boolean isResizedHandlerDisabled() {
		return resizedHandlerDisabled;
	}

	/**
	 * Determine whether or not the map should automatically react and resize when it receives a resize event.
	 * 
	 * @param resizedHandlerDisabled
	 *            true or false
	 */
	public void setResizedHandlerDisabled(boolean resizedHandlerDisabled) {
		this.resizedHandlerDisabled = resizedHandlerDisabled;
	}

	// -------------------------------------------------------------------------
	// Getters and setters:
	// -------------------------------------------------------------------------

	/**
	 * Apply a new <code>GraphicsController</code> on the map. This controller will handle all mouse-events that are
	 * global for the map. Only one controller can be set at any given time.
	 * 
	 * @param controller
	 *            The new <code>MapController</code> object.
	 * @since 1.6.0
	 */
	@Api
	public void setController(GraphicsController controller) {
		graphics.setController(controller);
	}

	/**
	 * Set a new mouse wheel controller on the map. If the zoom on scroll is currently enabled, it will be disabled
	 * first.
	 * 
	 * @param controller
	 *            The new mouse wheel controller to be applied on the map.
	 * @since 1.6.0
	 */
	@Api
	public void setMouseWheelController(MouseWheelHandler controller) {
		setZoomOnScrollEnabled(false);
		mouseWheelRegistration = graphics.addMouseWheelHandler(controller);
	}

	/**
	 * An optional fallbackController to return to, when no controller is explicitly set (controller=null). If no
	 * current controller is active when this setter is called, it is applied immediately. The default fall-back
	 * controller when a map is initialized, is the {@link PanController}, which allows you to navigate.
	 * 
	 * @param fallbackController
	 *            The new fall-back controller to use.
	 * @since 1.7.0
	 */
	@Api
	public void setFallbackController(GraphicsController fallbackController) {
		graphics.setFallbackController(fallbackController);
	}

	public double getUnitLength() {
		return unitLength;
	}

	public double getPixelLength() {
		return pixelLength;
	}

	/**
	 * Returns the number of pixels per map unit.
	 * 
	 * @return length of a pixel expressed in map units
	 * @since 1.7.0
	 */
	@Api
	public double getPixelPerUnit() {
		return pixelLength / unitLength;
	}

	/**
	 * Get the map's inner model. This model contains all the layers, handles selection, etc.
	 * 
	 * @return map model
	 * @since 1.6.0
	 */
	@Api
	public MapModel getMapModel() {
		return mapModel;
	}

	/**
	 * Get the context that handles right mouse clicks.
	 * 
	 * @return menu context
	 * @since 1.6.0
	 */
	@Api
	public MenuContext getMenuContext() {
		return graphics.getMenuContext();
	}

	/**
	 * Get the drawing context for rendering in general. If you are not using the render method, this would be an
	 * alternative - for advanced users only.
	 * 
	 * @return vector context
	 * @since 1.6.0
	 */
	@Api
	public GraphicsContext getVectorContext() {
		return graphics.getVectorContext();
	}

	/**
	 * Get the drawing context for raster layer rendering. If you are not using the render method, this would be an
	 * alternative - for advanced users only.
	 * 
	 * @return raster context
	 * @since 1.6.0
	 */
	@Api
	public ImageContext getRasterContext() {
		return graphics.getRasterContext();
	}

	public ShapeStyle getLineSelectStyle() {
		FeaturePainter painter = getFeaturePainter();
		if (painter != null) {
			return painter.getLineSelectStyle();
		}
		return null;
	}

	public void setLineSelectStyle(ShapeStyle lineSelectStyle) {
		FeaturePainter painter = getFeaturePainter();
		if (painter != null) {
			painter.setLineSelectStyle(lineSelectStyle);
		}
	}

	public ShapeStyle getPointSelectStyle() {
		FeaturePainter painter = getFeaturePainter();
		if (painter != null) {
			return painter.getPointSelectStyle();
		}
		return null;
	}

	public void setPointSelectStyle(ShapeStyle pointSelectStyle) {
		FeaturePainter painter = getFeaturePainter();
		if (painter != null) {
			painter.setPointSelectStyle(pointSelectStyle);
		}
	}

	public ShapeStyle getPolygonSelectStyle() {
		FeaturePainter painter = getFeaturePainter();
		if (painter != null) {
			return painter.getPolygonSelectStyle();
		}
		return null;
	}

	public void setPolygonSelectStyle(ShapeStyle polygonSelectStyle) {
		FeaturePainter painter = getFeaturePainter();
		if (painter != null) {
			painter.setPolygonSelectStyle(polygonSelectStyle);
		}
	}

	public Menu getDefaultMenu() {
		return defaultMenu;
	}

	public void setDefaultMenu(Menu defaultMenu) {
		this.defaultMenu = defaultMenu;
	}

	public String getApplicationId() {
		return applicationId;
	}

	// -------------------------------------------------------------------------
	// MapViewChangedHandler implementation:
	// -------------------------------------------------------------------------

	/** If the map view changes, redraw the map model and the scale bar. */
	public void onMapViewChanged(MapViewChangedEvent event) {
		if (isDrawn()) {
			if (scaleBarEnabled) {
				ScaleBar scalebar = (ScaleBar) addons.get("scalebar");
				if (scalebar != null) {
					scalebar.adjustScale(mapModel.getMapView().getCurrentScale());
					render(scalebar, RenderGroup.SCREEN, RenderStatus.UPDATE);
				}
			}
			render(mapModel, null, RenderStatus.ALL);
		}
	}

	// -------------------------------------------------------------------------
	// MapModelHandler implementation:
	// -------------------------------------------------------------------------

	/** When the initialization of the map's model is done: render it. */
	public void onMapModelChange(MapModelEvent event) {
		if (event.isLayerOrderChanged()) {
			render(mapModel, null, RenderStatus.DELETE);
			render(mapModel, null, RenderStatus.ALL);
		} else {
			render(mapModel, null, RenderStatus.ALL);
		}
	}

	// -------------------------------------------------------------------------
	// Private methods:
	// -------------------------------------------------------------------------

	private FeaturePainter getFeaturePainter() {
		List<Painter> painters = painterVisitor.getPaintersForObject(new Feature());
		for (Painter painter : painters) {
			if (painter instanceof FeaturePainter) {
				return (FeaturePainter) painter;
			}
		}
		return null;
	}

	protected void onDraw() {
		super.onDraw();
		// must be called before anything else !
		render(mapModel, null, RenderStatus.ALL);
		final int width = getWidth();
		final int height = getHeight();
		mapModel.getMapView().setSize(width, height);
		init();
	}

	protected void initializationCallback(GetMapConfigurationResponse r) {
		if (r.getMapInfo() != null && !mapModel.isInitialized()) {
			ClientMapInfo info = r.getMapInfo();
			unitLength = info.getUnitLength();
			pixelLength = info.getPixelLength();
			graphics.setBackgroundColor(info.getBackgroundColor());
			mapModel.initialize(info);
			setNavigationAddonEnabled(info.isPanButtonsEnabled());
			setScalebarEnabled(info.isScaleBarEnabled());
			painterVisitor.registerPainter(new FeaturePainter(new ShapeStyle(info.getPointSelectStyle()),
					new ShapeStyle(info.getLineSelectStyle()), new ShapeStyle(info.getPolygonSelectStyle())));

			for (final Layer<?> layer : mapModel.getLayers()) {
				layer.addLayerChangedHandler(new LayerChangedHandler() {

					public void onLabelChange(LayerLabeledEvent event) {
						render(layer, null, RenderStatus.ALL);
					}

					public void onVisibleChange(LayerShownEvent event) {
						render(layer, null, RenderStatus.ALL);
					}
				});
			}

			// Register the watermark MapAddon:
			Watermark watermark = new Watermark(id + "-watermark", this);
			watermark.setAlignment(Alignment.RIGHT);
			watermark.setVerticalAlignment(VerticalAlignment.BOTTOM);
			registerMapAddon(watermark);
		}
	}

	// -------------------------------------------------------------------------
	// Private ResizedHandler class
	// -------------------------------------------------------------------------

	/**
	 * Handles map view and scalebar on resize
	 */
	private class MapResizedHandler implements ResizedHandler {

		private MapWidget map;

		public MapResizedHandler(MapWidget map) {
			this.map = map;
		}

		public void onResized(ResizedEvent event) {
			if (!map.isResizedHandlerDisabled()) {
				try {
					final int width = map.getWidth();
					final int height = map.getHeight();
					mapModel.getMapView().setSize(width, height);

					for (String addonId : addons.keySet()) {
						MapAddon addon = addons.get(addonId);
						addon.setMapSize(width, height);
						render(addon, RenderGroup.SCREEN, RenderStatus.UPDATE);
					}
				} catch (Exception e) {
					GWT.log("OnResized exception", e);
				}
			}
		}
	}

	/**
	 * Controller that allows for zooming when scrolling the mouse wheel.
	 * 
	 * Default the controller will scroll retaining mouse position.
	 * 
	 * @author Pieter De Graef
	 * @author Oliver May
	 */
	private class ZoomOnScrollController implements MouseWheelHandler {

		private ScrollZoomType zoomType = ScrollZoomType.ZOOM_POSITION;

		public void onMouseWheel(MouseWheelEvent event) {
			if (event.isNorth()) {
				if (zoomType == ScrollZoomType.ZOOM_POSITION) {
					mapModel.getMapView().scale(
							2.0f,
							MapView.ZoomOption.LEVEL_CHANGE,
							mapModel.getMapView().getWorldViewTransformer()
									.viewToWorld(new Coordinate(event.getX(), event.getY())));
				} else {
					mapModel.getMapView().scale(2.0f, MapView.ZoomOption.LEVEL_CHANGE);
				}
			} else {
				if (zoomType == ScrollZoomType.ZOOM_POSITION) {
					mapModel.getMapView().scale(
							0.5f,
							MapView.ZoomOption.LEVEL_CHANGE,
							mapModel.getMapView().getWorldViewTransformer()
									.viewToWorld(new Coordinate(event.getX(), event.getY())));
				} else {
					mapModel.getMapView().scale(0.5f, MapView.ZoomOption.LEVEL_CHANGE);
				}
			}
		}
	}

	/**
	 * Renders feature on select/deselect
	 */
	private class MapWidgetFeatureSelectionHandler implements FeatureSelectionHandler {

		private MapWidget mapWidget;

		public MapWidgetFeatureSelectionHandler(MapWidget mapWidget) {
			this.mapWidget = mapWidget;
		}

		public void onFeatureSelected(FeatureSelectedEvent event) {
			mapWidget.render(event.getFeature(), RenderGroup.SCREEN, RenderStatus.UPDATE);
		}

		public void onFeatureDeselected(FeatureDeselectedEvent event) {
			mapWidget.render(event.getFeature(), RenderGroup.SCREEN, RenderStatus.DELETE);
		}
	}
}