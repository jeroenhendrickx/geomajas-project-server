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

package org.geomajas.gwt.client.command;

import org.geomajas.command.CommandResponse;
import org.geomajas.global.Api;
import org.geomajas.global.GeomajasConstant;
import org.geomajas.gwt.client.GeomajasService;
import org.geomajas.gwt.client.GeomajasServiceAsync;
import org.geomajas.gwt.client.command.event.DispatchStartedEvent;
import org.geomajas.gwt.client.command.event.DispatchStartedHandler;
import org.geomajas.gwt.client.command.event.DispatchStoppedEvent;
import org.geomajas.gwt.client.command.event.DispatchStoppedHandler;
import org.geomajas.gwt.client.command.event.HasDispatchHandlers;
import org.geomajas.gwt.client.i18n.I18nProvider;
import org.geomajas.gwt.client.widget.ExceptionWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.smartgwt.client.core.Function;
import com.smartgwt.client.util.SC;

/**
 * The central client side dispatcher for all commands. Use the {@link #execute(GwtCommand, CommandCallback...)}
 * function to execute an asynchronous command on the server.
 * 
 * @author Pieter De Graef
 * @since 1.6.0
 */
@Api(allMethods = true)
public final class GwtCommandDispatcher implements HasDispatchHandlers {

	private static GwtCommandDispatcher INSTANCE;

	private GeomajasServiceAsync service;

	private HandlerManager manager = new HandlerManager(this);

	private int nrOfDispatchedCommands;

	private String locale;

	private String userToken;

	private boolean useLazyLoading;

	private int lazyFeatureIncludesDefault;

	private int lazyFeatureIncludesSelect;

	private int lazyFeatureIncludesAll;
	
	private boolean consoleEnabled;

	private GwtCommandDispatcher() {
		locale = LocaleInfo.getCurrentLocale().getLocaleName();
		if ("default".equals(locale)) {
			locale = null;
		}
		service = (GeomajasServiceAsync) GWT.create(GeomajasService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) service;
		String moduleRelativeURL = GWT.getModuleBaseURL() + "geomajasService";
		endpoint.setServiceEntryPoint(moduleRelativeURL);
		setUseLazyLoading(true);
		setConsoleEnabled(true);
	}

	// -------------------------------------------------------------------------
	// Public methods:
	// -------------------------------------------------------------------------

	/**
	 * Get the only static instance of this class. This should be the object you work with.
	 * 
	 * @return singleton instance
	 */
	public static GwtCommandDispatcher getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new GwtCommandDispatcher();
		}
		return INSTANCE;
	}

	public HandlerRegistration addDispatchStartedHandler(DispatchStartedHandler handler) {
		return manager.addHandler(DispatchStartedEvent.getType(), handler);
	}

	public HandlerRegistration addDispatchStoppedHandler(DispatchStoppedHandler handler) {
		return manager.addHandler(DispatchStoppedEvent.getType(), handler);
	}

	/**
	 * The execution function. Executes a server side command.
	 * 
	 * @param command
	 *            The command to be executed. This command is a wrapper around the actual request object.
	 * @param onSuccess
	 *            A <code>CommandCallback</code> function to be executed when the command successfully returns.
	 * @return deferred object which can be used to add extra callbacks
	 */
	public Deferred execute(GwtCommand command, final CommandCallback... onSuccess) {
		incrementDispatched();

		final Deferred deferred = new Deferred();
		for (CommandCallback callback : onSuccess) {
			deferred.addSuccessCallback(callback);
		}

		command.setLocale(locale);
		command.setUserToken(userToken);
		service.execute(command, new AsyncCallback<CommandResponse>() {

			public void onFailure(Throwable error) {
				try {
					for (Function callback : deferred.getOnErrorCallbacks()) {
						callback.execute();
					}
					GWT.log(I18nProvider.getGlobal().commandError() + ":\n" + error.getMessage(), null);
					if (isConsoleEnabled()) {
						SC.warn(I18nProvider.getGlobal().commandError() + ":\n" + error.getMessage(), null);
					}
				} catch (Throwable t) {
					GWT.log("Command failed on error callback", t);
				} finally {
					decrementDispatched();
				}
			}

			public void onSuccess(CommandResponse response) {
				try {
					if (response.isError()) {
						String message = I18nProvider.getGlobal().commandError() + ":";
						for (String error : response.getErrorMessages()) {
							message += "\n" + error;
						}
						GWT.log(message, null);
						if (response.getExceptions() == null || response.getExceptions().size() == 0) {
							if (isConsoleEnabled()) {
								SC.warn(message, null);
							}
						} else {
							if (isConsoleEnabled()) {
								// The error messaging window only supports 1 exception to display:
								ExceptionWindow window = new ExceptionWindow(response.getExceptions().get(0));
								window.show();
							}
						}
					} else {
						if (!deferred.isCancelled()) {
							for (CommandCallback callback : deferred.getOnSuccessCallbacks()) {
								callback.execute(response);
							}
						}
					}
				} catch (Throwable t) {
					GWT.log("Command failed on success callback", t);
				} finally {
					decrementDispatched();
				}
			}
		});
		return deferred;
	}

	/**
	 * Is the dispatcher busy ?
	 * 
	 * @return true if there are outstanding commands
	 */
	public boolean isBusy() {
		return nrOfDispatchedCommands != 0;
	}

	/**
	 * Set the user token, so it can be sent in very command.
	 * 
	 * @param userToken
	 *            user token
	 */
	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}

	/**
	 * Is lazy feature loading enabled ?
	 * 
	 * @return true when lazy feature loading is enabled
	 */
	public boolean isUseLazyLoading() {
		return useLazyLoading;
	}

	/**
	 * Set lazy feature loading status.
	 * 
	 * @param useLazyLoading
	 *            lazy feature loading status
	 */
	public void setUseLazyLoading(boolean useLazyLoading) {
		if (useLazyLoading != this.useLazyLoading) {
			if (useLazyLoading) {
				lazyFeatureIncludesDefault = GeomajasConstant.FEATURE_INCLUDE_STYLE
						+ GeomajasConstant.FEATURE_INCLUDE_LABEL;
				lazyFeatureIncludesSelect = GeomajasConstant.FEATURE_INCLUDE_ALL;
				lazyFeatureIncludesAll = GeomajasConstant.FEATURE_INCLUDE_ALL;
			} else {
				lazyFeatureIncludesDefault = GeomajasConstant.FEATURE_INCLUDE_ALL;
				lazyFeatureIncludesSelect = GeomajasConstant.FEATURE_INCLUDE_ALL;
				lazyFeatureIncludesAll = GeomajasConstant.FEATURE_INCLUDE_ALL;
			}
		}
		this.useLazyLoading = useLazyLoading;
	}

	/**
	 * Get default value for "featureIncludes" when getting features.
	 * 
	 * @return default "featureIncludes" value
	 */
	public int getLazyFeatureIncludesDefault() {
		return lazyFeatureIncludesDefault;
	}

	/**
	 * Set default value for "featureIncludes" when getting features.
	 * 
	 * @param lazyFeatureIncludesDefault
	 *            default for "featureIncludes"
	 */
	public void setLazyFeatureIncludesDefault(int lazyFeatureIncludesDefault) {
		setUseLazyLoading(false);
		this.lazyFeatureIncludesDefault = lazyFeatureIncludesDefault;
	}

	/**
	 * Get "featureIncludes" to use when selecting features.
	 * 
	 * @return default "featureIncludes" for select commands
	 */
	public int getLazyFeatureIncludesSelect() {
		return lazyFeatureIncludesSelect;
	}

	/**
	 * Set default "featureIncludes" for select commands.
	 * 
	 * @param lazyFeatureIncludesSelect
	 *            default "featureIncludes" for select commands
	 */
	public void setLazyFeatureIncludesSelect(int lazyFeatureIncludesSelect) {
		setUseLazyLoading(false);
		this.lazyFeatureIncludesSelect = lazyFeatureIncludesSelect;
	}

	/**
	 * Value to use for "featureIncludes" when all should be included.
	 * 
	 * @return value for "featureIncludes" when all should be included
	 */
	public int getLazyFeatureIncludesAll() {
		return lazyFeatureIncludesAll;
	}

	/**
	 * Set "featureIncludes" value when all should be included.
	 * 
	 * @param lazyFeatureIncludesAll
	 *            "featureIncludes" value when all should be included
	 */
	public void setLazyFeatureIncludesAll(int lazyFeatureIncludesAll) {
		setUseLazyLoading(false);
		this.lazyFeatureIncludesAll = lazyFeatureIncludesAll;
	}
	
	/**
	 * Should the dispatcher send messages to the console ?
	 * 
	 * @return true if sending messages to console, false otherwise
	 * @since 1.9.0
	 */
	public boolean isConsoleEnabled() {
		return consoleEnabled;
	}

	/**
	 * Sets whether the dispatcher should send messages to the console.
	 * 
	 * @param consoleEnabled true if sending messages to console, false otherwise
	 * @since 1.9.0
	 */
	public void setConsoleEnabled(boolean consoleEnabled) {
		this.consoleEnabled = consoleEnabled;
	}

	// -------------------------------------------------------------------------
	// Protected methods:
	// -------------------------------------------------------------------------

	
	protected void incrementDispatched() {
		boolean started = nrOfDispatchedCommands == 0;
		nrOfDispatchedCommands++;
		if (started) {
			manager.fireEvent(new DispatchStartedEvent());
		}
	}

	protected void decrementDispatched() {
		nrOfDispatchedCommands--;
		if (nrOfDispatchedCommands == 0) {
			manager.fireEvent(new DispatchStoppedEvent());
		}
	}

}
