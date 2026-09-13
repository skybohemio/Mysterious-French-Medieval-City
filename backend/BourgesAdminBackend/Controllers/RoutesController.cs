using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using BourgesAdminBackend.Data;
using BourgesAdminBackend.Models;

namespace BourgesAdminBackend.Controllers
{
    public class RoutesController : Controller
    {
        private readonly BourgesDataContext _context;

        public RoutesController(BourgesDataContext context)
        {
            _context = context;
        }

        // GET: Routes
        public async Task<IActionResult> Index()
        {
            var routes = await _context.Routes.ToListAsync();
            var sitesDict = await _context.Sites.ToDictionaryAsync(s => s.Id, s => s.Title);

            ViewBag.SitesDict = sitesDict;
            return View(routes);
        }

        // GET: Routes/Create
        public async Task<IActionResult> Create()
        {
            var sites = await _context.Sites.OrderBy(s => s.Id).ToListAsync();
            ViewBag.AllSites = sites;

            return View("CreateEdit", new TourRoute());
        }

        // GET: Routes/Edit/5
        public async Task<IActionResult> Edit(int id)
        {
            var route = await _context.Routes.FindAsync(id);
            if (route == null)
            {
                return NotFound();
            }

            var sites = await _context.Sites.OrderBy(s => s.Id).ToListAsync();
            ViewBag.AllSites = sites;

            return View("CreateEdit", route);
        }

        // POST: Routes/Save
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Save(TourRoute route)
        {
            if (ModelState.IsValid)
            {
                // Format the SiteIdsString nicely to make sure it's valid JSON
                var ids = route.SiteIds; // parses the string
                route.SiteIds = ids;     // serializes it back cleanly as JSON e.g. "[1,2,4]"

                if (route.Id == 0)
                {
                    _context.Add(route);
                }
                else
                {
                    _context.Update(route);
                }
                await _context.SaveChangesAsync();
                TempData["SuccessMessage"] = $"Route '{route.NameFr}' saved successfully!";
                return RedirectToAction(nameof(Index));
            }

            var sites = await _context.Sites.OrderBy(s => s.Id).ToListAsync();
            ViewBag.AllSites = sites;
            TempData["ErrorMessage"] = "Please correct the errors in the form and try again.";
            return View("CreateEdit", route);
        }

        // GET: Routes/Delete/5
        public async Task<IActionResult> Delete(int id)
        {
            var route = await _context.Routes.FindAsync(id);
            if (route == null)
            {
                return NotFound();
            }

            var sitesDict = await _context.Sites.ToDictionaryAsync(s => s.Id, s => s.Title);
            ViewBag.SitesDict = sitesDict;

            return View(route);
        }

        // POST: Routes/DeleteConfirmed/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var route = await _context.Routes.FindAsync(id);
            if (route != null)
            {
                _context.Routes.Remove(route);
                await _context.SaveChangesAsync();
                TempData["SuccessMessage"] = $"Route '{route.NameFr}' deleted successfully.";
            }
            return RedirectToAction(nameof(Index));
        }
    }
}
