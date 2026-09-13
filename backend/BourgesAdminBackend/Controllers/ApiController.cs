using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using BourgesAdminBackend.Data;
using BourgesAdminBackend.Models;
using Newtonsoft.Json;
using System.Text;

namespace BourgesAdminBackend.Controllers
{
    [ApiController]
    [Route("api")]
    public class ApiController : ControllerBase
    {
        private readonly BourgesDataContext _context;

        public ApiController(BourgesDataContext context)
        {
            _context = context;
        }

        // GET: api/sites
        [HttpGet("sites")]
        public async Task<IActionResult> GetSites()
        {
            var sites = await _context.Sites.OrderBy(s => s.Id).ToListAsync();
            return Ok(sites);
        }

        // GET: api/routes
        [HttpGet("routes")]
        public async Task<IActionResult> GetRoutes()
        {
            var routes = await _context.Routes.OrderBy(s => s.Id).ToListAsync();
            // Map the siteIds list property so it serializes properly if needed, 
            // though standard Newtonsoft handles NotMapped or properties nicely depending on settings.
            var result = routes.Select(r => new {
                r.Id,
                r.NameFr,
                r.NameEn,
                r.NameDe,
                r.NameEs,
                r.NameNl,
                r.DescriptionFr,
                r.DescriptionEn,
                r.DescriptionDe,
                r.DescriptionEs,
                r.DescriptionNl,
                r.SiteIds, // returns list of ints
                r.ColorHex,
                r.DurationMin
            });
            return Ok(result);
        }

        // GET: api/export/json
        [HttpGet("export/json")]
        public async Task<IActionResult> ExportJson()
        {
            var sites = await _context.Sites.OrderBy(s => s.Id).ToListAsync();
            var routes = await _context.Routes.OrderBy(s => s.Id).ToListAsync();

            var dataPackage = new
            {
                ExportTimestamp = DateTime.UtcNow.ToString("o"),
                SitesCount = sites.Count,
                RoutesCount = routes.Count,
                Sites = sites,
                Routes = routes.Select(r => new {
                    r.Id,
                    r.NameFr,
                    r.NameEn,
                    r.NameDe,
                    r.NameEs,
                    r.NameNl,
                    r.DescriptionFr,
                    r.DescriptionEn,
                    r.DescriptionDe,
                    r.DescriptionEs,
                    r.DescriptionNl,
                    r.SiteIds,
                    r.ColorHex,
                    r.DurationMin
                })
            };

            var json = JsonConvert.SerializeObject(dataPackage, Formatting.Indented);
            var bytes = Encoding.UTF8.GetBytes(json);
            return File(bytes, "application/json", "bourges_guide_data.json");
        }

        // GET: api/export/sql
        [HttpGet("export/sql")]
        public async Task<IActionResult> ExportSql()
        {
            var sites = await _context.Sites.OrderBy(s => s.Id).ToListAsync();
            var routes = await _context.Routes.OrderBy(s => s.Id).ToListAsync();

            var sb = new StringBuilder();
            sb.AppendLine("-- Bourges Audio Guide - Administrative Database Export");
            sb.AppendLine($"-- Generated at: {DateTime.UtcNow} UTC");
            sb.AppendLine();
            sb.AppendLine("DELETE FROM routes;");
            sb.AppendLine("DELETE FROM sites;");
            sb.AppendLine();

            foreach (var site in sites)
            {
                var isPresetInt = site.IsPreset ? 1 : 0;
                sb.AppendLine($"INSERT INTO sites (id, title, description, narrationText, latitude, longitude, category, isPreset, audioDurationSec, titleFr, titleEn, titleDe, titleEs, titleNl, descriptionFr, descriptionEn, descriptionDe, descriptionEs, descriptionNl, narrationFr, narrationEn, narrationDe, narrationEs, narrationNl) VALUES ({site.Id}, '{EscapeSql(site.Title)}', '{EscapeSql(site.Description)}', '{EscapeSql(site.NarrationText)}', {site.Latitude}, {site.Longitude}, '{EscapeSql(site.Category)}', {isPresetInt}, {site.AudioDurationSec}, '{EscapeSql(site.TitleFr)}', '{EscapeSql(site.TitleEn)}', '{EscapeSql(site.TitleDe)}', '{EscapeSql(site.TitleEs)}', '{EscapeSql(site.TitleNl)}', '{EscapeSql(site.DescriptionFr)}', '{EscapeSql(site.DescriptionEn)}', '{EscapeSql(site.DescriptionDe)}', '{EscapeSql(site.DescriptionEs)}', '{EscapeSql(site.DescriptionNl)}', '{EscapeSql(site.NarrationFr)}', '{EscapeSql(site.NarrationEn)}', '{EscapeSql(site.NarrationDe)}', '{EscapeSql(site.NarrationEs)}', '{EscapeSql(site.NarrationNl)}');");
            }

            sb.AppendLine();

            foreach (var route in routes)
            {
                sb.AppendLine($"INSERT INTO routes (id, nameFr, nameEn, nameDe, nameEs, nameNl, descriptionFr, descriptionEn, descriptionDe, descriptionEs, descriptionNl, siteIds, colorHex, durationMin) VALUES ({route.Id}, '{EscapeSql(route.NameFr)}', '{EscapeSql(route.NameEn)}', '{EscapeSql(route.NameDe)}', '{EscapeSql(route.NameEs)}', '{EscapeSql(route.NameNl)}', '{EscapeSql(route.DescriptionFr)}', '{EscapeSql(route.DescriptionEn)}', '{EscapeSql(route.DescriptionDe)}', '{EscapeSql(route.DescriptionEs)}', '{EscapeSql(route.DescriptionNl)}', '{EscapeSql(route.SiteIdsString)}', '{EscapeSql(route.ColorHex)}', {route.DurationMin});");
            }

            var sqlText = sb.ToString();
            var bytes = Encoding.UTF8.GetBytes(sqlText);
            return File(bytes, "text/plain", "bourges_guide_insert.sql");
        }

        private string EscapeSql(string input)
        {
            if (string.IsNullOrEmpty(input)) return string.Empty;
            return input.Replace("'", "''");
        }
    }
}
