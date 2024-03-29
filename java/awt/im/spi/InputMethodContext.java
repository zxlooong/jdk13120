/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.awt.im.spi;

import java.awt.Window;
import java.awt.font.TextHitInfo;
import java.awt.im.InputMethodRequests;
import java.text.AttributedCharacterIterator;

/**
 * Provides methods that input methods
 * can use to communicate with their client components or to request
 * other services.
 *
 * @since 1.3
 *
 * @version 	1.10, 02/06/02
 * @author JavaSoft International
 */

public interface InputMethodContext extends InputMethodRequests {

    /**
     * Creates an input method event from the arguments given
     * and dispatches it to the client component. For arguments,
     * see {@link java.awt.event.InputMethodEvent#InputMethodEvent}.
     */
    public void dispatchInputMethodEvent(int id,
                AttributedCharacterIterator text, int committedCharacterCount,
                TextHitInfo caret, TextHitInfo visiblePosition);

    /**
     * Creates a top-level window for use by the input method.
     * The intended behavior of this window is:
     * <ul>
     * <li>it floats above all document windows and dialogs
     * <li>it and all components that it contains do not receive the focus
     * <li>it has lightweight decorations, such as a reduced drag region without title
     * </ul>
     * However, the actual behavior with respect to these three items is platform dependent.
     * <p>
     * The title may or may not be displayed, depending on the actual type of window created.
     * <p>
     * If attachToInputContext is true, the new window will share the input context that
     * corresponds to this input method context, so that events for components in the window
     * are automatically dispatched to the input method.
     * Also, when the window is opened using setVisible(true), the input context will prevent
     * deactivate and activate calls to the input method that might otherwise be caused.
     * <p>
     * @param title the title to be displayed in the window's title bar,
     * if there is such a title bar.
     * A <code>null</code> value is treated as an empty string, "".
     * @param attachToInputContext whether this window should share the input context
     * that corresponds to this input method context 
     * @return a window with special characteristics for use by input methods
     */
    public Window createInputMethodWindow(String title, boolean attachToInputContext);

    /**
     * Enables or disables notification of the current client window's
     * location and state for the specified input method. When
     * notification is enabled, the input method's {@link
     * java.awt.im.spi.InputMethod#notifyClientWindowChange
     * notifyClientWindowChange} method is called as described in that
     * method's specification. Notification is automatically disabled
     * when the input method is disposed.
     *
     * @param inputMethod the input method for which notifications are
     * enabled or disabled
     * @param enable true to enable, false to disable
     */
    public void enableClientWindowNotification(InputMethod inputMethod, boolean enable);
}
