{
"openapi": "3.0.0",
"info": {
"title": "Echo Panda API",
"version": "1.0.0"
},
"servers": [
{
"url": "http://127.0.0.1:8000",
"description": "Local server"
}
],
"paths": {
"/api/test": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/test",
"operationId": "testGetApiTest",
"responses": {
"200": {
"description": "OK"
}
}
}
},
"/api/register": {
"post": {
"tags": [
"Auth"
],
"summary": "POST /api/register",
"operationId": "registerPostApiRegister",
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"name",
"email",
"password"
],
"properties": {
"name": {
"type": "string",
"maxLength": 255
},
"email": {
"type": "string",
"format": "email",
"maxLength": 255
},
"password": {
"type": "string",
"minLength": 8
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"201": {
"description": "Created"
}
}
}
},
"/api/login": {
"post": {
"tags": [
"Auth"
],
"summary": "POST /api/login",
"operationId": "loginPostApiLogin",
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"email",
"password"
],
"properties": {
"email": {
"type": "string",
"format": "email"
},
"password": {
"type": "string"
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
}
}
}
},
"/api/firebase/login": {
"post": {
"tags": [
"Auth"
],
"summary": "POST /api/firebase/login",
"operationId": "firebaseLoginPostApiFirebaseLogin",
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"email"
],
"properties": {
"email": {
"type": "string",
"format": "email"
},
"name": {
"type": "string",
"nullable": true,
"maxLength": 255
},
"firebase_uid": {
"type": "string",
"nullable": true,
"maxLength": 255
},
"provider": {
"type": "string",
"nullable": true,
"maxLength": 50
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
}
}
}
},
"/api/products": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/products",
"operationId": "indexGetApiProducts",
"parameters": [
{
"name": "search",
"in": "query",
"required": false,
"schema": {
"type": "string"
}
},
{
"name": "is_active",
"in": "query",
"required": false,
"schema": {
"type": "boolean"
}
},
{
"name": "per_page",
"in": "query",
"required": false,
"schema": {
"type": "integer"
}
}
],
"responses": {
"200": {
"description": "OK"
}
}
},
"post": {
"tags": [
"Admin"
],
"summary": "POST /api/products",
"operationId": "storePostApiProducts",
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"name",
"price",
"quantity"
],
"properties": {
"name": {
"type": "string",
"maxLength": 255
},
"description": {
"type": "string",
"nullable": true
},
"price": {
"type": "number",
"minimum": 0
},
"quantity": {
"type": "integer",
"minimum": 0
},
"sku": {
"type": "string",
"nullable": true,
"maxLength": 255
},
"is_active": {
"type": "boolean",
"nullable": true
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"201": {
"description": "Created"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/products/{product}": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/products/{product}",
"operationId": "showGetApiProductsProduct",
"parameters": [
{
"name": "product",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"responses": {
"200": {
"description": "OK"
}
}
},
"put": {
"tags": [
"Admin"
],
"summary": "PUT /api/products/{product}",
"operationId": "updatePutApiProductsProduct",
"parameters": [
{
"name": "product",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"name",
"price",
"quantity"
],
"properties": {
"name": {
"type": "string",
"maxLength": 255
},
"description": {
"type": "string",
"nullable": true
},
"price": {
"type": "number",
"minimum": 0
},
"quantity": {
"type": "integer",
"minimum": 0
},
"sku": {
"type": "string",
"nullable": true,
"maxLength": 255
},
"is_active": {
"type": "boolean",
"nullable": true
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
},
"delete": {
"tags": [
"Admin"
],
"summary": "DELETE /api/products/{product}",
"operationId": "destroyDeleteApiProductsProduct",
"parameters": [
{
"name": "product",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/albums": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/albums",
"operationId": "indexGetApiAlbums",
"parameters": [
{
"name": "search",
"in": "query",
"required": false,
"schema": {
"type": "string"
}
},
{
"name": "sort_by",
"in": "query",
"required": false,
"schema": {
"type": "string",
"enum": [
"latest",
"oldest"
]
}
},
{
"name": "per_page",
"in": "query",
"required": false,
"schema": {
"type": "integer"
}
}
],
"responses": {
"200": {
"description": "OK"
}
}
},
"post": {
"tags": [
"Artist"
],
"summary": "POST /api/albums",
"operationId": "storePostApiAlbums",
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"title",
"artist"
],
"properties": {
"title": {
"type": "string",
"maxLength": 255
},
"artist": {
"type": "string",
"maxLength": 255
},
"release_date": {
"type": "string",
"format": "date",
"nullable": true
},
"description": {
"type": "string",
"nullable": true
},
"release_status": {
"type": "string",
"nullable": true,
"enum": [
"draft",
"pending_review",
"published",
"rejected"
]
},
"scheduled_at": {
"type": "string",
"format": "date-time",
"nullable": true
},
"cover_key": {
"type": "string",
"nullable": true,
"maxLength": 1024
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"201": {
"description": "Created"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/albums/{album}": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/albums/{album}",
"operationId": "showGetApiAlbumsAlbum",
"parameters": [
{
"name": "album",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"responses": {
"200": {
"description": "OK"
}
}
},
"put": {
"tags": [
"Artist"
],
"summary": "PUT /api/albums/{album}",
"operationId": "updatePutApiAlbumsAlbum",
"parameters": [
{
"name": "album",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"title",
"artist"
],
"properties": {
"title": {
"type": "string",
"maxLength": 255
},
"artist": {
"type": "string",
"maxLength": 255
},
"release_date": {
"type": "string",
"format": "date",
"nullable": true
},
"description": {
"type": "string",
"nullable": true
},
"release_status": {
"type": "string",
"nullable": true,
"enum": [
"draft",
"pending_review",
"published",
"rejected"
]
},
"scheduled_at": {
"type": "string",
"format": "date-time",
"nullable": true
},
"cover_key": {
"type": "string",
"nullable": true,
"maxLength": 1024
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
},
"delete": {
"tags": [
"Artist"
],
"summary": "DELETE /api/albums/{album}",
"operationId": "destroyDeleteApiAlbumsAlbum",
"parameters": [
{
"name": "album",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/albums/{albumId}/songs": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/albums/{albumId}/songs",
"operationId": "getByAlbumGetApiAlbumsAlbumIdSongs",
"parameters": [
{
"name": "albumId",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
},
{
"name": "per_page",
"in": "query",
"required": false,
"schema": {
"type": "integer"
}
}
],
"responses": {
"200": {
"description": "OK"
}
}
}
},
"/api/albums/{album}/cover-url": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/albums/{album}/cover-url",
"operationId": "coverUrlGetApiAlbumsAlbumCoverUrl",
"parameters": [
{
"name": "album",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"responses": {
"200": {
"description": "OK"
}
}
}
},
"/api/songs": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/songs",
"operationId": "indexGetApiSongs",
"parameters": [
{
"name": "search",
"in": "query",
"required": false,
"schema": {
"type": "string"
}
},
{
"name": "album_id",
"in": "query",
"required": false,
"schema": {
"type": "integer"
}
},
{
"name": "sort_by",
"in": "query",
"required": false,
"schema": {
"type": "string",
"enum": [
"track_number",
"latest"
]
}
},
{
"name": "per_page",
"in": "query",
"required": false,
"schema": {
"type": "integer"
}
}
],
"responses": {
"200": {
"description": "OK"
}
}
},
"post": {
"tags": [
"Artist"
],
"summary": "POST /api/songs",
"operationId": "storePostApiSongs",
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"album_id",
"title",
"duration",
"track_number"
],
"properties": {
"album_id": {
"type": "integer"
},
"title": {
"type": "string",
"maxLength": 255
},
"artist": {
"type": "string",
"nullable": true,
"maxLength": 255
},
"duration": {
"type": "integer",
"minimum": 1
},
"track_number": {
"type": "integer",
"minimum": 1
},
"lyrics": {
"type": "string",
"nullable": true
},
"original_key": {
"type": "string",
"nullable": true,
"maxLength": 1024
},
"cover_key": {
"type": "string",
"nullable": true,
"maxLength": 1024
},
"preview_key": {
"type": "string",
"nullable": true,
"maxLength": 1024
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"201": {
"description": "Created"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/songs/{song}": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/songs/{song}",
"operationId": "showGetApiSongsSong",
"parameters": [
{
"name": "song",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"responses": {
"200": {
"description": "OK"
}
}
},
"put": {
"tags": [
"Artist"
],
"summary": "PUT /api/songs/{song}",
"operationId": "updatePutApiSongsSong",
"parameters": [
{
"name": "song",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"album_id",
"title",
"duration",
"track_number"
],
"properties": {
"album_id": {
"type": "integer"
},
"title": {
"type": "string",
"maxLength": 255
},
"artist": {
"type": "string",
"nullable": true,
"maxLength": 255
},
"duration": {
"type": "integer",
"minimum": 1
},
"track_number": {
"type": "integer",
"minimum": 1
},
"lyrics": {
"type": "string",
"nullable": true
},
"original_key": {
"type": "string",
"nullable": true,
"maxLength": 1024
},
"cover_key": {
"type": "string",
"nullable": true,
"maxLength": 1024
},
"preview_key": {
"type": "string",
"nullable": true,
"maxLength": 1024
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
},
"delete": {
"tags": [
"Artist"
],
"summary": "DELETE /api/songs/{song}",
"operationId": "destroyDeleteApiSongsSong",
"parameters": [
{
"name": "song",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/stats/most-played-songs": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/stats/most-played-songs",
"operationId": "mostPlayedSongsGetApiStatsMostPlayedSongs",
"parameters": [
{
"name": "limit",
"in": "query",
"required": false,
"schema": {
"type": "integer"
}
}
],
"responses": {
"200": {
"description": "OK"
}
}
}
},
"/api/stats/most-played-albums": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/stats/most-played-albums",
"operationId": "mostPlayedAlbumsGetApiStatsMostPlayedAlbums",
"parameters": [
{
"name": "limit",
"in": "query",
"required": false,
"schema": {
"type": "integer"
}
}
],
"responses": {
"200": {
"description": "OK"
}
}
}
},
"/api/artists": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/artists",
"operationId": "indexGetApiArtists",
"responses": {
"200": {
"description": "OK"
}
}
}
},
"/api/artists/{artist}/image-url": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/artists/{artist}/image-url",
"operationId": "imageUrlGetApiArtistsArtistImageUrl",
"parameters": [
{
"name": "artist",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"responses": {
"200": {
"description": "OK"
}
}
}
},
"/api/logout": {
"post": {
"tags": [
"Auth"
],
"summary": "POST /api/logout",
"operationId": "logoutPostApiLogout",
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/me": {
"get": {
"tags": [
"Auth"
],
"summary": "GET /api/me",
"operationId": "meGetApiMe",
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/users/by-role": {
"get": {
"tags": [
"Admin"
],
"summary": "GET /api/users/by-role",
"operationId": "usersByRoleGetApiUsersByRole",
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/profile": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/profile",
"operationId": "showGetApiProfile",
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
},
"put": {
"tags": [
"User"
],
"summary": "PUT /api/profile",
"operationId": "updatePutApiProfile",
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"name",
"email"
],
"properties": {
"name": {
"type": "string",
"maxLength": 255
},
"email": {
"type": "string",
"format": "email",
"maxLength": 255
},
"image_url": {
"type": "string",
"nullable": true,
"maxLength": 2048
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/profile/favorite-songs": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/profile/favorite-songs",
"operationId": "getFavoriteSongsGetApiProfileFavoriteSongs",
"parameters": [
{
"name": "per_page",
"in": "query",
"required": false,
"schema": {
"type": "integer"
}
}
],
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/profile/favorite-albums": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/profile/favorite-albums",
"operationId": "getFavoriteAlbumsGetApiProfileFavoriteAlbums",
"parameters": [
{
"name": "per_page",
"in": "query",
"required": false,
"schema": {
"type": "integer"
}
}
],
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/upload/media/presign": {
"post": {
"tags": [
"Artist"
],
"summary": "POST /api/upload/media/presign",
"operationId": "presignMediaPostApiUploadMediaPresign",
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"purpose",
"filename",
"content_type",
"size"
],
"properties": {
"purpose": {
"type": "string",
"enum": [
"album_cover",
"song_audio",
"artist_image",
"song_lyrics"
]
},
"filename": {
"type": "string",
"maxLength": 255
},
"content_type": {
"type": "string",
"maxLength": 255
},
"size": {
"type": "integer",
"minimum": 1
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/upload/media": {
"post": {
"tags": [
"Artist"
],
"summary": "POST /api/upload/media",
"operationId": "mediaPostApiUploadMedia",
"requestBody": {
"required": true,
"content": {
"multipart/form-data": {
"schema": {
"required": [
"file",
"purpose"
],
"properties": {
"file": {
"type": "string",
"format": "binary"
},
"purpose": {
"type": "string",
"enum": [
"album_cover",
"song_audio",
"artist_image",
"song_lyrics"
]
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
},
"delete": {
"tags": [
"Artist"
],
"summary": "DELETE /api/upload/media",
"operationId": "deleteMediaDeleteApiUploadMedia",
"requestBody": {
"description": "Provide either key or url.",
"required": true,
"content": {
"application/json": {
"schema": {
"required": [],
"properties": {
"key": {
"type": "string",
"nullable": true
},
"url": {
"type": "string",
"nullable": true
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/artist/analytics": {
"get": {
"tags": [
"Artist"
],
"summary": "GET /api/artist/analytics",
"operationId": "showGetApiArtistAnalytics",
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/artist/create": {
"post": {
"tags": [
"Artist"
],
"summary": "POST /api/artist/create",
"operationId": "storePostApiArtistCreate",
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"name"
],
"properties": {
"name": {
"type": "string",
"maxLength": 255
},
"image_url": {
"type": "string",
"nullable": true,
"maxLength": 2048
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/favorites": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/favorites",
"operationId": "indexGetApiFavorites",
"parameters": [
{
"name": "type",
"in": "query",
"required": false,
"schema": {
"type": "string",
"enum": [
"song",
"album"
]
}
}
],
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/favorites/songs": {
"post": {
"tags": [
"User"
],
"summary": "POST /api/favorites/songs",
"operationId": "addSongPostApiFavoritesSongs",
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"song_id"
],
"properties": {
"song_id": {
"type": "integer"
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"201": {
"description": "Created"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/favorites/albums": {
"post": {
"tags": [
"User"
],
"summary": "POST /api/favorites/albums",
"operationId": "addAlbumPostApiFavoritesAlbums",
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"album_id"
],
"properties": {
"album_id": {
"type": "integer"
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"201": {
"description": "Created"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/favorites/songs/check": {
"post": {
"tags": [
"User"
],
"summary": "POST /api/favorites/songs/check",
"operationId": "checkSongPostApiFavoritesSongsCheck",
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"song_id"
],
"properties": {
"song_id": {
"type": "integer"
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/favorites/albums/check": {
"post": {
"tags": [
"User"
],
"summary": "POST /api/favorites/albums/check",
"operationId": "checkAlbumPostApiFavoritesAlbumsCheck",
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"album_id"
],
"properties": {
"album_id": {
"type": "integer"
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/favorites/songs/remove": {
"post": {
"tags": [
"User"
],
"summary": "POST /api/favorites/songs/remove",
"operationId": "removeSongPostApiFavoritesSongsRemove",
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"song_id"
],
"properties": {
"song_id": {
"type": "integer"
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/favorites/albums/remove": {
"post": {
"tags": [
"User"
],
"summary": "POST /api/favorites/albums/remove",
"operationId": "removeAlbumPostApiFavoritesAlbumsRemove",
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"album_id"
],
"properties": {
"album_id": {
"type": "integer"
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/favorites/{favorite}": {
"delete": {
"tags": [
"User"
],
"summary": "DELETE /api/favorites/{favorite}",
"operationId": "destroyDeleteApiFavoritesFavorite",
"parameters": [
{
"name": "favorite",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/listen-history": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/listen-history",
"operationId": "myHistoryGetApiListenHistory",
"parameters": [
{
"name": "per_page",
"in": "query",
"required": false,
"schema": {
"type": "integer"
}
}
],
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
},
"post": {
"tags": [
"User"
],
"summary": "POST /api/listen-history",
"operationId": "trackPostApiListenHistory",
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"song_id"
],
"properties": {
"song_id": {
"type": "integer"
},
"duration_listened": {
"type": "integer",
"nullable": true,
"minimum": 0
},
"completed": {
"type": "boolean",
"nullable": true
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/songs/{song}/stream-ticket": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/songs/{song}/stream-ticket",
"operationId": "showGetApiSongsSongStreamTicket",
"parameters": [
{
"name": "song",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
},
{
"name": "quality",
"in": "query",
"required": false,
"schema": {
"type": "string",
"enum": [
"128",
"320"
]
}
}
],
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/playback/progress": {
"post": {
"tags": [
"User"
],
"summary": "POST /api/playback/progress",
"operationId": "progressPostApiPlaybackProgress",
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"song_id",
"progress_seconds",
"duration_seconds"
],
"properties": {
"song_id": {
"type": "integer"
},
"progress_seconds": {
"type": "integer",
"minimum": 0
},
"duration_seconds": {
"type": "integer",
"minimum": 1
},
"source": {
"type": "string",
"nullable": true,
"enum": [
"web",
"android"
]
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/playback/complete": {
"post": {
"tags": [
"User"
],
"summary": "POST /api/playback/complete",
"operationId": "completePostApiPlaybackComplete",
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"song_id",
"duration_seconds"
],
"properties": {
"song_id": {
"type": "integer"
},
"duration_seconds": {
"type": "integer",
"minimum": 1
},
"source": {
"type": "string",
"nullable": true,
"enum": [
"web",
"android"
]
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/playback/recent": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/playback/recent",
"operationId": "recentlyPlayedGetApiPlaybackRecent",
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/songs/{song}/lyrics": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/songs/{song}/lyrics",
"operationId": "showGetApiSongsSongLyrics",
"parameters": [
{
"name": "song",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/playlists": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/playlists",
"operationId": "indexGetApiPlaylists",
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
},
"post": {
"tags": [
"User"
],
"summary": "POST /api/playlists",
"operationId": "storePostApiPlaylists",
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"name"
],
"properties": {
"name": {
"type": "string",
"maxLength": 255
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"201": {
"description": "Created"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/playlists/{playlist}": {
"delete": {
"tags": [
"User"
],
"summary": "DELETE /api/playlists/{playlist}",
"operationId": "destroyDeleteApiPlaylistsPlaylist",
"parameters": [
{
"name": "playlist",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/playlists/{playlist}/songs": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/playlists/{playlist}/songs",
"operationId": "songsGetApiPlaylistsPlaylistSongs",
"parameters": [
{
"name": "playlist",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
},
"post": {
"tags": [
"User"
],
"summary": "POST /api/playlists/{playlist}/songs",
"operationId": "addSongPostApiPlaylistsPlaylistSongs",
"parameters": [
{
"name": "playlist",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"requestBody": {
"required": true,
"content": {
"application/json": {
"schema": {
"required": [
"song_id"
],
"properties": {
"song_id": {
"type": "integer"
}
},
"type": "object"
}
}
}
},
"responses": {
"200": {
"description": "OK"
},
"201": {
"description": "Created"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/playlists/{playlist}/songs/{song}": {
"delete": {
"tags": [
"User"
],
"summary": "DELETE /api/playlists/{playlist}/songs/{song}",
"operationId": "removeSongDeleteApiPlaylistsPlaylistSongsSong",
"parameters": [
{
"name": "playlist",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
},
{
"name": "song",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/playlists/{playlist}/songs/{song}/exists": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/playlists/{playlist}/songs/{song}/exists",
"operationId": "hasSongGetApiPlaylistsPlaylistSongsSongExists",
"parameters": [
{
"name": "playlist",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
},
{
"name": "song",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"responses": {
"200": {
"description": "OK"
},
"401": {
"description": "Unauthenticated"
},
"403": {
"description": "Forbidden"
}
},
"security": [
{
"sanctum": []
}
]
}
},
"/api/stream/{song}/{quality}": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/stream/{song}/{quality}",
"operationId": "streamGetApiStreamSongQuality",
"parameters": [
{
"name": "song",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
},
{
"name": "quality",
"in": "path",
"required": true,
"schema": {
"type": "string",
"enum": [
"128",
"320"
]
}
},
{
"name": "uid",
"in": "query",
"required": false,
"schema": {
"type": "integer"
}
}
],
"responses": {
"200": {
"description": "OK"
}
}
}
},
"/api/songs/{song}/signed-url": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/songs/{song}/signed-url",
"operationId": "signedUrlGetApiSongsSongSignedUrl",
"parameters": [
{
"name": "song",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"responses": {
"200": {
"description": "OK"
}
}
}
},
"/api/songs/{song}/cover-url": {
"get": {
"tags": [
"User"
],
"summary": "GET /api/songs/{song}/cover-url",
"operationId": "coverUrlGetApiSongsSongCoverUrl",
"parameters": [
{
"name": "song",
"in": "path",
"required": true,
"schema": {
"type": "string"
}
}
],
"responses": {
"200": {
"description": "OK"
}
}
}
}
},
"tags": [
{
"name": "User",
"description": "User"
},
{
"name": "Auth",
"description": "Auth"
},
{
"name": "Admin",
"description": "Admin"
},
{
"name": "Artist",
"description": "Artist"
}
]
}