using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using BourgesAdminBackend.Data;
using BourgesAdminBackend.Models;

namespace BourgesAdminBackend.Controllers
{
    public class SitesController : Controller
    {
        private readonly BourgesDataContext _context;

        public SitesController(BourgesDataContext context)
        {
            _context = context;
        }

        // GET: Sites
        public async Task<IActionResult> Index(string? search, string? category)
        {
            var query = _context.Sites.AsQueryable();

            if (!string.IsNullOrWhiteSpace(search))
            {
                query = query.Where(s => s.Title.Contains(search) || s.Description.Contains(search));
                ViewBag.Search = search;
            }

            if (!string.IsNullOrWhiteSpace(category) && category != "ALL")
            {
                query = query.Where(s => s.Category == category);
                ViewBag.Category = category;
            }

            var sites = await query.OrderBy(s => s.Id).ToListAsync();
            return View(sites);
        }

        // GET: Sites/Create
        public IActionResult Create()
        {
            return View("CreateEdit", new Site());
        }

        // GET: Sites/Edit/5
        public async Task<IActionResult> Edit(int id)
        {
            var site = await _context.Sites.FindAsync(id);
            if (site == null)
            {
                return NotFound();
            }
            return View("CreateEdit", site);
        }

        // POST: Sites/Save
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Save(Site site)
        {
            if (ModelState.IsValid)
            {
                if (site.Id == 0)
                {
                    // If saving for the first time, make sure default properties match French if not specified
                    if (string.IsNullOrWhiteSpace(site.TitleFr)) site.TitleFr = site.Title;
                    if (string.IsNullOrWhiteSpace(site.DescriptionFr)) site.DescriptionFr = site.Description;
                    if (string.IsNullOrWhiteSpace(site.NarrationFr)) site.NarrationFr = site.NarrationText;

                    _context.Add(site);
                }
                else
                {
                    _context.Update(site);
                }
                await _context.SaveChangesAsync();
                TempData["SuccessMessage"] = $"Site '{site.Title}' saved successfully!";
                return RedirectToAction(nameof(Index));
            }

            TempData["ErrorMessage"] = "Please correct the errors in the form and try again.";
            return View("CreateEdit", site);
        }

        // GET: Sites/Delete/5
        public async Task<IActionResult> Delete(int id)
        {
            var site = await _context.Sites.FindAsync(id);
            if (site == null)
            {
                return NotFound();
            }
            return View(site);
        }

        // POST: Sites/DeleteConfirmed/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var site = await _context.Sites.FindAsync(id);
            if (site != null)
            {
                _context.Sites.Remove(site);
                await _context.SaveChangesAsync();
                TempData["SuccessMessage"] = $"Site '{site.Title}' deleted successfully.";
            }
            return RedirectToAction(nameof(Index));
        }
    }
}
