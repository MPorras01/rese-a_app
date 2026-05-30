-- Agregar coordenadas geográficas a negocios
-- Usadas para mostrar en mapa con Leaflet + OpenStreetMap (Nominatim geocoding)
ALTER TABLE businesses
    ADD COLUMN IF NOT EXISTS lat  DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS lng  DOUBLE PRECISION;

-- Índice para búsquedas por bounding box (futuro)
CREATE INDEX IF NOT EXISTS idx_businesses_coords
    ON businesses (lat, lng)
    WHERE lat IS NOT NULL AND lng IS NOT NULL;
