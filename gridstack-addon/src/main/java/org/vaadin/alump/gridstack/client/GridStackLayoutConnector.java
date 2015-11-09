/**
 * GridStackLayoutConnector.java (GridStackLayout)
 *
 * Copyright 2015 Vaadin Ltd, Sami Viitanen <sami.viitanen@vaadin.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vaadin.alump.gridstack.client;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.Util;
import com.vaadin.client.ui.AbstractLayoutConnector;

import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.LayoutClickEventHandler;
import com.vaadin.shared.Connector;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.LayoutClickRpc;
import org.vaadin.alump.gridstack.client.shared.GridStackMoveData;
import org.vaadin.alump.gridstack.client.shared.GridStackServerRpc;
import org.vaadin.alump.gridstack.client.shared.GridStackLayoutState;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Connect(org.vaadin.alump.gridstack.GridStackLayout.class)
public class GridStackLayoutConnector extends AbstractLayoutConnector {

    private final static Logger LOGGER = Logger.getLogger(GridStackLayoutConnector.class.getName());

    @Override
    public void init() {
        super.init();
        getWidget().setMoveHandler(new GwtGridStack.GwtGridStackMoveHandler() {
            @Override
            public void onWidgetsMoved(Widget[] widgets, GwtGridStackChangedItem[] data) {
                List<GridStackMoveData> dataSent = new ArrayList<GridStackMoveData>();
                for(int i = 0; i < widgets.length; ++i) {
                    Widget widget = widgets[i];
                    GwtGridStackChangedItem itemData = data[i];
                    dataSent.add(new GridStackMoveData(getChildConnectorForWidget(widget),
                            itemData.getX(), itemData.getY(), itemData.getWidth(), itemData.getHeight()));
                }
                getRpcProxy(GridStackServerRpc.class).onChildrenMoved(dataSent);
            }
        });
    }

    protected ComponentConnector getChildConnectorForWidget(Widget widget) {
        for(ComponentConnector connector : getChildComponents()) {
            if(connector.getWidget() == widget) {
                return connector;
            }
        }
        return null;
    }

    @Override
    public void onUnregister() {
        super.onUnregister();
    }

	@Override
	public GwtGridStack getWidget() {
		return (GwtGridStack) super.getWidget();
	}

	@Override
	public GridStackLayoutState getState() {
		return (GridStackLayoutState) super.getState();
	}

	@Override
	public void onStateChanged(StateChangeEvent event) {
		super.onStateChanged(event);
        clickEventHandler.handleEventHandlerRegistration();

        if(event.isInitialStateChange() || event.hasPropertyChanged("gridStackProperties")) {
            getWidget().setOptions(getState().gridStackOptions.width, getState().gridStackOptions.height,
                    GwtGridStackOptions.createFrom(getState().gridStackOptions));
        }

        if(getWidget().isInitialized() && event.hasPropertyChanged("childOptions")) {
            getWidget().batchUpdate();
            for(Connector connector : getState().childOptions.keySet()) {
                Widget widget = ((ComponentConnector)connector).getWidget();
                getWidget().updateChild(widget, getState().childOptions.get(connector));
            }
            getWidget().commit();
        }
	}

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent event) {

        for (ComponentConnector child : event.getOldChildren()) {
            if (child.getParent() != this) {
                Widget widget = child.getWidget();
                if (widget.isAttached()) {
                    getWidget().remove(widget);
                }
            }
        }

        for (ComponentConnector child : getChildComponents()) {
            if (child.getWidget().getParent() != getWidget()) {
               getWidget().add(child.getWidget(), getState().childOptions.get(child));
            }
        }
    }

    @Override
    public void updateCaption(ComponentConnector componentConnector) {
        //ignore for now
    }

    private final LayoutClickEventHandler clickEventHandler = new LayoutClickEventHandler(this) {

        @Override
        protected ComponentConnector getChildComponent(Element element) {
            return Util.getConnectorForElement(getConnection(), getWidget(),
                    element);
        }

        @Override
        protected LayoutClickRpc getLayoutClickRPC() {
            return getRpcProxy(GridStackServerRpc.class);
        };

        @Override
        protected void fireClick(NativeEvent event) {
            // Because of event handling in js library, resize/dragging causes clicks to parent element
            if(getWidget().isClickOk()) {
                super.fireClick(event);
            }
        }
    };
}