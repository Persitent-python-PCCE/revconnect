# RevConnect Standalone Frontend

The main project also serves the same frontend from Spring Boot's `src/main/resources/static` folder.

To run this copy separately:

1. Start the Spring Boot backend on `http://localhost:8081`.
2. From this `frontend` folder run:
   `python -m http.server 5500`
3. Open `http://localhost:5500/register.html`.

`js/config.js` automatically points API calls to `http://localhost:8081` when served on port 5500. The backend includes development CORS support for ports 5500.
