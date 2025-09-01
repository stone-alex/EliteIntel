EDSM API Overview

Purpose: Provides system/station data, flight logs, and coordinates. Used by tools like EDDiscovery and EDMarketConnector.
Endpoints (from web results):

Journal API: POST https://www.edsm.net/api-journal-v1 for submitting journal events (not querying).
Systems API: GET https://www.edsm.net/api-v1/systems for system data (e.g., coordinates, factions).
Stations API: GET https://www.edsm.net/api-v1/stations for station details (e.g., services like material traders).
Logs API: GET https://www.edsm.net/api-logs-v1 for flight logs (e.g., system visits).
Rate Limit: 360 requests/hour (~1 per 10 seconds) per user (web:9).


API Key: Required for authenticated calls. You’ve got one, so you’re set.
Documentation: Scattered but available at:

https://www.edsm.net/en/api (general overview).
Specific endpoints like /api-journal-v1, /api-systems-v1, /api-logs-v1 (web:3,6,9,10,21).
Redfox’s guide for EDSM setup (web:0) suggests checking the user dropdown on www.edsm.net for API key details.



Key Endpoints for Your Use Case

Find Material Traders/Outfitting:

GET https://www.edsm.net/api-v1/stations?systemName=Sol&services=materialtrader
Parameters: systemName (current system from SystemSession), services (e.g., materialtrader, outfitting, shipyard).
Returns: JSON array of stations with matching services.


Find Commodities:

GET https://www.edsm.net/api-v1/stations/market?systemName=Sol&commodityName=Painite
Returns: Market data for specific commodities (less comprehensive than Spansh).


System Search:

GET https://www.edsm.net/api-v1/systems?systemName=Sol&radius=100
Returns: Nearby systems within a radius (useful for navigation).



Challenges

Documentation: Not centralized; you’ll need to test endpoints with your API key.
Data Scope: EDSM is great for systems and stations but weaker on commodity prices compared to Spansh or EDDB.
Rate Limits: 360/hour is decent but requires throttling for heavy use.