using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Newtonsoft.Json;

namespace BourgesAdminBackend.Models
{
    public class TourRoute
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        [Required]
        [StringLength(150)]
        public string NameFr { get; set; } = string.Empty;

        [Required]
        [StringLength(150)]
        public string NameEn { get; set; } = string.Empty;

        [Required]
        [StringLength(150)]
        public string NameDe { get; set; } = string.Empty;

        [Required]
        [StringLength(150)]
        public string NameEs { get; set; } = string.Empty;

        [Required]
        [StringLength(150)]
        public string NameNl { get; set; } = string.Empty;

        [Required]
        public string DescriptionFr { get; set; } = string.Empty;

        [Required]
        public string DescriptionEn { get; set; } = string.Empty;

        [Required]
        public string DescriptionDe { get; set; } = string.Empty;

        [Required]
        public string DescriptionEs { get; set; } = string.Empty;

        [Required]
        public string DescriptionNl { get; set; } = string.Empty;

        [Required]
        public string SiteIdsString { get; set; } = "[]"; // Serialized JSON array or comma separated, let's use a JSON array or simple comma separated

        [NotMapped]
        public List<int> SiteIds
        {
            get
            {
                if (string.IsNullOrWhiteSpace(SiteIdsString)) return new List<int>();
                try
                {
                    // Attempt to parse as JSON first (e.g. [1,2,3])
                    if (SiteIdsString.Trim().StartsWith("["))
                    {
                        return JsonConvert.DeserializeObject<List<int>>(SiteIdsString) ?? new List<int>();
                    }
                    // Fallback to comma separated (e.g. 1,2,3)
                    return SiteIdsString.Split(',', StringSplitOptions.RemoveEmptyEntries)
                                        .Select(int.Parse)
                                        .ToList();
                }
                catch
                {
                    return new List<int>();
                }
            }
            set
            {
                SiteIdsString = JsonConvert.SerializeObject(value ?? new List<int>());
            }
        }

        [Required]
        [StringLength(10)]
        public string ColorHex { get; set; } = "#FF5722";

        public int DurationMin { get; set; } = 30;
    }
}
