/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.documentsui.dirlist;

import android.content.Context;
import android.support.v13.view.DragStartHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnItemTouchListener;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.android.documentsui.Events.InputEvent;

//Receives event meant for both directory and empty view, and either pass them to
//{@link UserInputHandler} for simple gestures (Single Tap, Long-Press), or intercept them for
//other types of gestures (drag n' drop)
final class ListeningGestureDetector extends GestureDetector
        implements OnItemTouchListener, OnTouchListener {
    private final DragStartHelper mDragHelper;
    private final GestureMultiSelectHelper mGestureSelectHelper;

    public ListeningGestureDetector(
            Context context,
            DragStartHelper dragHelper,
            UserInputHandler<? extends InputEvent> handler,
            GestureMultiSelectHelper gestureMultiSelectHelper) {
        super(context, handler);
        mDragHelper = dragHelper;
        mGestureSelectHelper = gestureMultiSelectHelper;
        setOnDoubleTapListener(handler);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        // Detect drag events from mouse. When a drag is detected, intercept the rest of the
        // gesture.
        View itemView = rv.findChildViewUnder(e.getX(), e.getY());
        if (itemView != null && mDragHelper.onTouch(itemView, e)) {
            return true;
        }

        if (mGestureSelectHelper.onInterceptTouchEvent(rv, e)) {
            return true;
        }

        // Forward unhandled events to UserInputHandler.
        return onTouchEvent(e);
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        mGestureSelectHelper.onTouchEvent(rv, e);

        // Note: even though this event is being handled as part of gesture-multi select, continue
        // forwarding to the GestureDetector. The detector needs to see the entire cluster of events
        // in order to properly interpret other gestures, such as long press.
        onTouchEvent(e);
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}

    // For mEmptyView events
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Pass events to UserInputHandler.
        return onTouchEvent(event);
    }
}