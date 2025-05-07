package Controller.Maryem;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * A custom MapLayer for displaying a marker icon on a Gluon MapView.
 */
public class CustomMarkerLayer extends MapLayer {
    private final ImageView marker;
    private final MapView mapView;
    private MapPoint currentPosition;
    private static final double ICON_WIDTH = 40.0;
    private static final double ICON_HEIGHT = 47.0;
    private static final double ICON_ANCHOR_X = ICON_WIDTH / 2.0; // Center horizontally (x=50)
    private static final double ICON_ANCHOR_Y = ICON_HEIGHT; // Pin tip at bottom (y=100)

    /**
     * Constructs a CustomMarkerLayer with the specified MapView and initial position.
     * Loads a marker icon from Icons8 (https://img.icons8.com/?size=100&id=gh2uD53Hj8rj&format=png&color=000000).
     * Note: Icons8 requires a backlink to https://icons8.com or a paid license for free use.
     * @param mapView The MapView to display the marker on.
     * @param initialPosition The initial MapPoint for the marker.
     */
    public CustomMarkerLayer(MapView mapView, MapPoint initialPosition) {
        this.mapView = mapView;
        this.currentPosition = initialPosition;

        // Load marker icon from Icons8 URL
        Image icon = new Image("https://img.icons8.com/?size=100&id=gh2uD53Hj8rj&format=png&color=000000");
        marker = new ImageView(icon);
        marker.setFitWidth(ICON_WIDTH);
        marker.setFitHeight(ICON_HEIGHT);
        this.getChildren().add(marker);

        layoutLayer();
    }

    /**
     * Updates the marker's position to a new MapPoint.
     * @param newPosition The new position for the marker.
     */
    public void updateMarker(MapPoint newPosition) {
        currentPosition = newPosition;
        layoutLayer();
    }

    @Override
    protected void layoutLayer() {
        if (currentPosition == null || mapView == null) return;

        MapPoint center = mapView.getCenter();
        double zoom = mapView.getZoom();
        double width = mapView.getWidth();
        double height = mapView.getHeight();

        double tileSize = 256;
        double pixelsPerLonDegree = tileSize * Math.pow(2, zoom) / 360;
        double pixelsPerLonRadian = tileSize * Math.pow(2, zoom) / (2 * Math.PI);

        double latRad = Math.toRadians(currentPosition.getLatitude());
        double centerLatRad = Math.toRadians(center.getLatitude());

        double mercatorY = Math.log(Math.tan(Math.PI/4 + latRad/2));
        double centerMercatorY = Math.log(Math.tan(Math.PI/4 + centerLatRad/2));

        double x = (currentPosition.getLongitude() - center.getLongitude()) * pixelsPerLonDegree;
        double y = (centerMercatorY - mercatorY) * pixelsPerLonRadian;

        // Position the marker so the pin's tip (bottom center) is at the coordinates
        marker.setTranslateX(width/2 + x - ICON_ANCHOR_X);
        marker.setTranslateY(height/2 + y - ICON_ANCHOR_Y);
    }

    @Override
    public void layoutChildren() {
        super.layoutChildren();
        layoutLayer();
    }
}