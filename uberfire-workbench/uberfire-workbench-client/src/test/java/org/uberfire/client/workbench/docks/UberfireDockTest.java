package org.uberfire.client.workbench.docks;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwtmockito.GwtMockitoTestRunner;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

@RunWith(GwtMockitoTestRunner.class)
public class UberfireDockTest extends TestCase {

    private static final UberfireDockPosition DOCK_POSITION = UberfireDockPosition.EAST;
    private static String ICON_TYPE = "ICON_TYPE";
    private static final String SCREEN_ID = "SCREEN_ID";
    private static final String PERSPECTIVE_ID = "PERSPECTIVE_ID";

    private static final int SIZE = 450;
    private static final String LABEL = "DOCK TITLE";
    private static final String TOOLTIP = "DOCK TOOLTIP";

    @Mock
    private ImageResource imageIcon;

    @Mock
    private ImageResource imageIconFocused;

    private UberfireDock tested;
    private UberfireDock testedWithImages;

    @Before
    public void setup() {
        tested = new UberfireDock(DOCK_POSITION,
                                  ICON_TYPE,
                                  new DefaultPlaceRequest(SCREEN_ID),
                                  PERSPECTIVE_ID)
                .withLabel(LABEL)
                .withTooltip(TOOLTIP)
                .withSize(SIZE);

        testedWithImages = new UberfireDock(DOCK_POSITION,
                                            imageIcon,
                                            imageIconFocused,
                                            new DefaultPlaceRequest(SCREEN_ID),
                                            PERSPECTIVE_ID);
    }

    @Test
    public void testWithLabel() {
        assertTrue(tested.getLabel().compareTo(LABEL) == 0);
    }

    @Test
    public void testWithTooltip() {
        assertTrue(tested.getTooltip().compareTo(TOOLTIP) == 0);

        final UberfireDock tested2 = new UberfireDock(UberfireDockPosition.EAST,
                                                      ICON_TYPE,
                                                      new DefaultPlaceRequest(SCREEN_ID),
                                                      PERSPECTIVE_ID);
        assertNull(tested2.getTooltip());
    }

    @Test
    public void testWithSize() {
        assertEquals(SIZE, tested.getSize(), 0);
    }

    @Test
    public void testSetUberfireDockPosition() {
        assertEquals(DOCK_POSITION, tested.getDockPosition());
    }

    @Test
    public void testGetAssociatedPerspective() {
        assertEquals(PERSPECTIVE_ID, tested.getAssociatedPerspective());
    }

    @Test
    public void testGetIdentifier() {
        assertEquals(SCREEN_ID, tested.getIdentifier());
    }

    @Test
    public void testGetPlaceRequest() {
        assertEquals(SCREEN_ID, tested.getPlaceRequest().getIdentifier());
    }

    @Test
    public void testGetDockPosition() {
        assertEquals(DOCK_POSITION, tested.getDockPosition());
    }

    @Test
    public void testGetSize() {
        assertEquals(SIZE, tested.getSize(), 0);
    }

    @Test
    public void testGetLabel() {
        assertEquals(LABEL, tested.getLabel());
    }

    @Test
    public void testGetTooltip() {
        assertEquals(TOOLTIP, tested.getTooltip());
    }

    @Test
    public void testGetIconType() {
        assertEquals(ICON_TYPE, tested.getIconType());
    }

    @Test
    public void testGetImageIcon() {
        assertEquals(imageIcon, testedWithImages.getImageIcon());
    }

    @Test
    public void testGetImageIconFocused() {
        assertEquals(imageIconFocused, testedWithImages.getImageIconFocused());
    }

    @Test
    public void testEquals() {
        UberfireDock compareDock1 = new UberfireDock(DOCK_POSITION,
                                                     ICON_TYPE,
                                                     new DefaultPlaceRequest(SCREEN_ID),
                                                     PERSPECTIVE_ID)
                .withLabel(LABEL)
                .withTooltip(TOOLTIP)
                .withSize(SIZE);

        UberfireDock compareDock2 = new UberfireDock(DOCK_POSITION,
                                                     ICON_TYPE,
                                                     new DefaultPlaceRequest(SCREEN_ID),
                                                     PERSPECTIVE_ID);

        assertTrue(tested.equals(compareDock1));
        assertFalse(tested.equals(compareDock2));
    }

    @Test
    public void testTestHashCode() {
        UberfireDock compareDock1 = new UberfireDock(DOCK_POSITION,
                                                     ICON_TYPE,
                                                     new DefaultPlaceRequest(SCREEN_ID),
                                                     PERSPECTIVE_ID)
                .withLabel(LABEL)
                .withTooltip(TOOLTIP)
                .withSize(SIZE);

        UberfireDock compareDock2 = new UberfireDock(DOCK_POSITION,
                                                     ICON_TYPE,
                                                     new DefaultPlaceRequest(SCREEN_ID),
                                                     PERSPECTIVE_ID);

        assertEquals(tested.hashCode(), compareDock1.hashCode());
        assertNotSame(tested.hashCode(), compareDock2.hashCode());
    }
}