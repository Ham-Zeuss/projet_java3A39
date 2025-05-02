package Controller.Maryem;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class MapApplication extends Application {

    private Label latLabel;
    private Label lngLabel;
    private MapPoint markerPosition;
    private CustomMarkerLayer markerLayer;
    private MapView mapView;

    @Override
    public void start(Stage primaryStage) {
        // Create MapView
        mapView = new MapView();
        mapView.setPrefSize(800, 500);

        // Set initial position (Tunis, Tunisia) and zoom level
        markerPosition = new MapPoint(36.8065, 10.1815);
        mapView.setCenter(markerPosition);
        mapView.setZoom(15);

        // Add a custom layer with a marker
        markerLayer = new CustomMarkerLayer(mapView, markerPosition);
        mapView.addLayer(markerLayer);

        // Labels for coordinates
        latLabel = new Label("Latitude: " + markerPosition.getLatitude());
        lngLabel = new Label("Longitude: " + markerPosition.getLongitude());

        // Add mouse click handler to pin marker
        mapView.setOnMouseClicked(event -> {
            // Only process single clicks (not drags)
            if (event.isStillSincePress()) {
                // Get the clicked position as a MapPoint
                MapPoint clickedPoint = mapView.getMapPosition(event.getX(), event.getY());
                markerPosition = clickedPoint;
                markerLayer.updateMarker(markerPosition);

                // Update labels
                updateCoordinateLabels();
                System.out.println("Marker set to: Lat=" + markerPosition.getLatitude() +
                        ", Lon=" + markerPosition.getLongitude());
            }
        });

        // Layout
        VBox root = new VBox(mapView, latLabel, lngLabel);
        Scene scene = new Scene(root, 800, 500);

        // Stage setup
        primaryStage.setTitle("Gluon Maps Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateCoordinateLabels() {
        latLabel.setText("Latitude: " + String.format("%.6f", markerPosition.getLatitude()));
        lngLabel.setText("Longitude: " + String.format("%.6f", markerPosition.getLongitude()));
    }

    // Custom layer to display a marker
    private class CustomMarkerLayer extends MapLayer {
        private final Circle marker;
        private final MapView mapView;
        private MapPoint currentPosition;

        public CustomMarkerLayer(MapView mapView, MapPoint initialPosition) {
            this.mapView = mapView;
            this.currentPosition = initialPosition;

            // Create the marker
            marker = new Circle(10, Color.RED);
            marker.setStroke(Color.BLACK);
            marker.setStrokeWidth(2);
            this.getChildren().add(marker);

            // Initial layout
            layoutLayer();

            // Add listener for map changes by overriding the layoutChildren method
            // This will be called whenever the map needs to redraw
        }

        public void updateMarker(MapPoint newPosition) {
            currentPosition = newPosition;
            layoutLayer();
        }

        @Override
        protected void layoutLayer() {
            if (currentPosition == null || mapView == null) return;

            // Get current map state
            MapPoint center = mapView.getCenter();
            double zoom = mapView.getZoom();
            double width = mapView.getWidth();
            double height = mapView.getHeight();

            // Calculate pixel coordinates
            double tileSize = 256;
            double pixelsPerLonDegree = tileSize * Math.pow(2, zoom) / 360;
            double pixelsPerLonRadian = tileSize * Math.pow(2, zoom) / (2 * Math.PI);

            // Convert marker position to screen coordinates
            double latRad = Math.toRadians(currentPosition.getLatitude());
            double centerLatRad = Math.toRadians(center.getLatitude());

            double mercatorY = Math.log(Math.tan(Math.PI/4 + latRad/2));
            double centerMercatorY = Math.log(Math.tan(Math.PI/4 + centerLatRad/2));

            double x = (currentPosition.getLongitude() - center.getLongitude()) * pixelsPerLonDegree;
            double y = (centerMercatorY - mercatorY) * pixelsPerLonRadian;

            // Position the marker
            marker.setCenterX(width/2 + x);
            marker.setCenterY(height/2 + y);
        }

        @Override
        public void layoutChildren() {
            super.layoutChildren();
            layoutLayer(); // This ensures the marker is repositioned when the map changes
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}