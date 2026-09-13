using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using BourgesAdminBackend.Data;
using BourgesAdminBackend.Models;
using System.Diagnostics;

namespace BourgesAdminBackend.Controllers
{
    public class HomeController : Controller
    {
        private readonly BourgesDataContext _context;

        public HomeController(BourgesDataContext context)
        {
            _context = context;
        }

        public async Task<IActionResult> Index()
        {
            var totalSites = await _context.Sites.CountAsync();
            var totalRoutes = await _context.Routes.CountAsync();

            var categoryCounts = await _context.Sites
                .GroupBy(s => s.Category)
                .Select(g => new { Category = g.Key, Count = g.Count() })
                .ToDictionaryAsync(x => x.Category, x => x.Count);

            var presetCount = await _context.Sites.CountAsync(s => s.IsPreset);
            var customCount = totalSites - presetCount;

            ViewBag.TotalSites = totalSites;
            ViewBag.TotalRoutes = totalRoutes;
            ViewBag.CategoryCounts = categoryCounts;
            ViewBag.PresetCount = presetCount;
            ViewBag.CustomCount = customCount;

            // Get recently added/edited sites
            var recentSites = await _context.Sites
                .OrderByDescending(s => s.Id)
                .Take(5)
                .ToListAsync();

            return View(recentSites);
        }

        public async Task<IActionResult> Export()
        {
            var sites = await _context.Sites.ToListAsync();
            var routes = await _context.Routes.ToListAsync();

            ViewBag.SitesCount = sites.Count;
            ViewBag.RoutesCount = routes.Count;

            return View();
        }

        [ResponseCache(Duration = 0, Location = ResponseCacheLocation.None, NoStore = true)]
        public IActionResult Error()
        {
            return View(new ErrorViewModel { RequestId = Activity.Current?.Id ?? HttpContext.TraceIdentifier });
        }
    }

    // Small nested model for errors if not defined elsewhere
    public class ErrorViewModel
    {
        public string? RequestId { get; set; }
        public bool ShowRequestId => !string.IsNullOrEmpty(RequestId);
    }
}
